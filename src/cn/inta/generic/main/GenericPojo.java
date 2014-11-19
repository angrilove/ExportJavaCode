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
 * 生成POJO文件
 * 
 * @author Anshen
 *
 */
public class GenericPojo {

	private final static String indent = "\t";

	public static void gen() {
		try {
			Connection connection = GeneratorUtils.getConnection();

			String tableSchema = GeneratorUtils.getProperty("tableschema");
			String basePackage = GeneratorUtils.getProperty("basepackage");
			String pojoPackage = basePackage + ".pojo";
			boolean removeTablePrefix = StringUtils.equalsIgnoreCase("true",
					GeneratorUtils.getProperty("removetableprefix")) ? true : false;
			File dir = new File((tableSchema + "." + pojoPackage).replace(".",
					"\\"));
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
						" ");

				String fileName = tableSchema + "\\"
						+ pojoPackage.replace(".", "\\") + "\\" + className
						+ ".java";
				File file = new File(fileName);
				if (file.exists()) {
					file.delete();
				}
				file.createNewFile();
				StringBuffer contentBuffer = new StringBuffer();
				contentBuffer.append("package ").append(pojoPackage)
						.append(";\n\nimport java.util.*;\n\n")
						.append("/**\n * ").append(className).append(" ")
						.append(tableComment).append("\n * @author Anshen\n")
						.append(" */\n").append("public class ")
						.append(className).append(" {\n\n");
				StringBuffer getterSetterBuffer = new StringBuffer();
				String csql = "select COLUMN_NAME,DATA_TYPE,COLUMN_COMMENT from columns where TABLE_SCHEMA = ? and TABLE_NAME = ?";
				PreparedStatement pstmColumns = connection
						.prepareStatement(csql);
				pstmColumns.setString(1, tableSchema);
				pstmColumns.setString(2, tableName);
				pstmColumns.execute();
				ResultSet columns = pstmColumns.getResultSet();
				while (columns.next()) {
					String propertyName = WordUtils.capitalize(
							columns.getString("COLUMN_NAME").replace("-", " ")
									.replace("_", " ")).replace(" ", "");
					if (propertyName.equalsIgnoreCase("return"))
						propertyName += propertyName + "0";
					String fieldName = StringUtils.uncapitalize(propertyName);
					String dataType = columns.getString("DATA_TYPE");
					String columnComment = columns.getString("COLUMN_COMMENT");
					if (dataType.contains("int")) {
						contentBuffer.append(indent).append("private Integer ")
								.append(fieldName).append("; // ")
								.append(columnComment).append("\n");
						getterSetterBuffer.append(indent)
								.append("public Integer get")
								.append(propertyName).append("() {\n")
								.append(indent).append(indent)
								.append("return ").append(fieldName)
								.append(";\n").append(indent).append("}\n")
								.append(indent).append("public void set")
								.append(propertyName).append("(")
								.append("Integer ").append(fieldName)
								.append(") {\n").append(indent).append(indent)
								.append("this.").append(fieldName)
								.append(" = ").append(fieldName).append(";\n")
								.append(indent).append("}\n");
					} else if (dataType.contains("double")) {
						contentBuffer.append(indent).append("private Double ")
								.append(fieldName).append("; // ")
								.append(columnComment).append("\n");
						getterSetterBuffer.append(indent)
								.append("public Double get")
								.append(propertyName).append("() {\n")
								.append(indent).append(indent)
								.append("return ").append(fieldName)
								.append(";\n").append(indent).append("}\n")
								.append(indent).append("public void set")
								.append(propertyName).append("(")
								.append("Double ").append(fieldName)
								.append(") {\n").append(indent).append(indent)
								.append("this.").append(fieldName)
								.append(" = ").append(fieldName).append(";\n")
								.append(indent).append("}\n");
					} else if (dataType.contains("time")
							|| dataType.contains("date")) {
						contentBuffer.append(indent).append("private Date ")
								.append(fieldName).append("; // ")
								.append(columnComment).append("\n");
						getterSetterBuffer.append(indent)
								.append("public Date get").append(propertyName)
								.append("() {\n").append(indent).append(indent)
								.append("return ").append(fieldName)
								.append(";\n").append(indent).append("}\n")
								.append(indent).append("public void set")
								.append(propertyName).append("(")
								.append("Date ").append(fieldName)
								.append(") {\n").append(indent).append(indent)
								.append("this.").append(fieldName)
								.append(" = ").append(fieldName).append(";\n")
								.append(indent).append("}\n");
					} else {
						contentBuffer.append(indent).append("private String ")
								.append(fieldName).append("; // ")
								.append(columnComment).append("\n");
						getterSetterBuffer.append(indent)
								.append("public String get")
								.append(propertyName).append("() {\n")
								.append(indent).append(indent)
								.append("return ").append(fieldName)
								.append(";\n").append(indent).append("}\n")
								.append(indent).append("public void set")
								.append(propertyName).append("(")
								.append("String ").append(fieldName)
								.append(") {\n").append(indent).append(indent)
								.append("this.").append(fieldName)
								.append(" = ").append(fieldName).append(";\n")
								.append(indent).append("}\n");
					}
				}
				columns.close();
				contentBuffer.append("\n").append(getterSetterBuffer)
						.append("}\n");
				String content = "";
				System.out.println("table name => "+tableName);
				if (tableName.equals("v9_tag"))
					System.out.println(contentBuffer.toString());
				content = JavaCodeFormatter.format(contentBuffer.toString());
				write(file, content);
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

	private static void write(File file, String content) throws IOException {
		FileOutputStream fos = new FileOutputStream(file);
		fos.write(content.getBytes());
		fos.close();
	}

}
