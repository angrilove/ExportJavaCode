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

/**
 * 
 * 生成Service Implement文件
 * 
 * @author Anshen
 *
 */
public class GenericServiceImpl {
	
	public static void gen() {
		String tabSpaces = "    ";
		try {
			String tableSchema = GeneratorUtils.getProperty("tableschema");
			String basePackage = GeneratorUtils.getProperty("basepackage");
			String implPackage = basePackage + ".service.impl";
			boolean removeTablePrefix = StringUtils.equalsIgnoreCase("true", GeneratorUtils.getProperty("tableschema")) ? true : false;
			File dir = new File((tableSchema + "." + implPackage).replace(".", "\\"));
			if (dir.exists()) {
				dir.delete();
			}
			dir.mkdirs();

			String sql = "select TABLE_NAME,TABLE_COMMENT from tables where TABLE_SCHEMA = ?";
			Connection connection = GeneratorUtils.getConnection();
			PreparedStatement pstm = connection.prepareStatement(sql);
			pstm.setString(1, tableSchema);
			ResultSet rs = GeneratorUtils.query(pstm);
			while (rs.next()) {
				String tableName = rs.getString("TABLE_NAME").toLowerCase();
				String tableComment = rs.getString("TABLE_COMMENT");
				String[] splits = tableName.replace("-", " ").replace("_", " ").split(" ");
				if (removeTablePrefix) {
					splits = ArrayUtils.subarray(splits, 1, splits.length);
				}
				String className = StringUtils.remove(WordUtils.capitalize(StringUtils.join(splits, " ")), " ") + "ServiceImpl";
				String fileName = tableSchema + "\\" + implPackage.replace(".", "\\") + "\\" + className + ".java";
				File file = new File(fileName);
				if (file.exists()) {
					file.delete();
				}
				file.createNewFile();
				StringBuffer contentBuffer = new StringBuffer();
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
					} else if (columns.getString(1).contains("date") || columns.getString(1).contains("time")) {
						keyType = "Date";
					}
				}
				contentBuffer.append("package ")
					.append(basePackage).append(".service.impl;\n\nimport ").append(basePackage)
					.append(".pojo.").append(className.replace("ServiceImpl", "")).append(";\n")
					.append("import ").append(basePackage).append(".service.").append(className.replace("Impl", "")).append(";\n")
					.append("import org.springframework.stereotype.Service;\n")
					.append("import org.springframework.transaction.annotation.Transactional;\n")
					.append("import javax.annotation.PostConstruct;\n")
					.append("import com.ingta.framework.ibatis.service.impl.IbatisBaseServiceImpl;\n\n")
					.append("/**\n * ").append(className).append(" ")
					.append(tableComment).append(" 业务实现\n * @author Anshen\n")
					.append(" */\n").append("@Service(\"").append(className.replace("Impl", "")).append("\")\n@Transactional(readOnly = true)\npublic class ")
					.append(className).append(" extends IbatisBaseServiceImpl<")
					.append(className.replace("ServiceImpl", "")).append(", ").append(keyType).append("> implements ")
					.append(className.replace("Impl", "")).append(" {\n\n").append(tabSpaces)
					.append("@PostConstruct\n").append(tabSpaces).append("public void init() {\n")
					.append(tabSpaces).append(tabSpaces).append("ibatisDao.init(")
					.append(className.replace("ServiceImpl", "")).append(".class, ")
					.append(keyType).append(".class);\n").append(tabSpaces).append("}\n");

				FileOutputStream fos = new FileOutputStream(file);
				contentBuffer.append("}\n");
				fos.write(contentBuffer.toString().getBytes());
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
