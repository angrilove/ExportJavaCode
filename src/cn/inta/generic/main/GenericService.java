/*
 * Copyright (C) 2014.
 * Write by Anshen
 * All Right Reseved.
 * 
 */
package cn.inta.generic.main;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;

import cn.inta.generic.util.GeneratorUtils;
import cn.inta.generic.util.JavaCodeFormatter;

/**
 * 
 * 生成Service Interface文件
 * 
 * @author Anshen
 *
 */
public class GenericService {

	public static void gen() {
		try {
			Connection connection = GeneratorUtils.getConnection();

			String tableSchema = GeneratorUtils.getProperty("tableschema");
			String basePackage = GeneratorUtils.getProperty("basepackage");
			String servicePackage = basePackage + ".service";
			boolean removeTablePrefix = StringUtils.equalsIgnoreCase("true",
					GeneratorUtils.getProperty("tableschema")) ? true : false;
			File dir = new File((tableSchema + "." + servicePackage).replace(
					".", "\\"));
			if (dir.exists()) {
				dir.delete();
			}
			dir.mkdirs();

			String sql = "select TABLE_NAME,TABLE_COMMENT from tables where TABLE_SCHEMA = ?";
			PreparedStatement pstm = connection.prepareStatement(sql);
			pstm.setString(1, tableSchema);
			ResultSet rs = GeneratorUtils.query(pstm);
			while (rs.next()) {
				String tableName = rs.getString("TABLE_NAME").toLowerCase();
				String tableComment = rs.getString("TABLE_COMMENT");
				String[] splits = tableName.replace("-", " ").replace("_", " ")
						.split(" ");
				if (removeTablePrefix) {
					splits = ArrayUtils.subarray(splits, 1, splits.length);
				}
				String className = StringUtils.remove(
						WordUtils.capitalize(StringUtils.join(splits, " ")),
						" ") + "Service";
				String fileName = tableSchema + "\\"
						+ servicePackage.replace(".", "\\") + "\\" + className
						+ ".java";
				File file = new File(fileName);
				if (file.exists()) {
					file.delete();
				}
				file.createNewFile();
				sql = "select DATA_TYPE from columns where TABLE_SCHEMA = ? and TABLE_NAME = ? and COLUMN_KEY = ?";
				pstm = connection.prepareStatement(sql);
				pstm.setString(1, "escpt");
				pstm.setString(2, tableName);
				pstm.setString(3, "PRI");
				ResultSet columns = GeneratorUtils.query(pstm);
				String keyType = "String";
				while (columns.next()) {
					if (columns.getString(1).contains("int")) {
						keyType = "Integer";
						break;
					} else if (columns.getString(1).contains("double")) {
						keyType = "Double";
					} else if (columns.getString(1).contains("date")
							|| columns.getString(1).contains("time")) {
						keyType = "Date";
					}
				}
				StringBuffer contentBuffer = new StringBuffer();
				contentBuffer
						.append("package ")
						.append(servicePackage)
						.append(";\n\nimport ")
						.append(basePackage)
						.append(".pojo.")
						.append(className.replace("Service", ""))
						.append(";\nimport com.ingta.framework.ibatis.service.IbatisBaseService;\n\n")
						.append("/**\n * ").append(className).append(" ")
						.append(tableComment)
						.append(" 业务逻辑\n * @author Anshen\n").append(" */\n")
						.append("public interface ").append(className)
						.append(" extends IbatisBaseService<")
						.append(className.replace("Service", "")).append(", ")
						.append(keyType).append("> {\n\n");

				FileOutputStream fos = new FileOutputStream(file);
				contentBuffer.append("}\n");
				String content = "";
				try {
					content = JavaCodeFormatter.format(contentBuffer.toString());
				} catch (Exception e) {
					e.printStackTrace();
				}
				fos.write(content.getBytes());
				fos.close();
			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			GeneratorUtils.closeConnection();
		}
	}
}
