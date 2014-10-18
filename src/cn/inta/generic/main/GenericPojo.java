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
 * 生成POJO文件
 * 
 * @author Anshen
 *
 */
public class GenericPojo {
	
	public static void gen() {
		String tabSpaces = "    ";
		try {
			Connection connection = GeneratorUtils.getConnection();

			String tableSchema = GeneratorUtils.getProperty("tableschema");
			String basePackage = GeneratorUtils.getProperty("basepackage");
			String pojoPackage = basePackage + ".pojo";
			boolean removeTablePrefix = StringUtils.equalsIgnoreCase("true", GeneratorUtils.getProperty("tableschema")) ? true : false;
			File dir = new File((tableSchema + "." + pojoPackage).replace(".", "\\"));
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
				String[] splits = tableName.replace("-", " ").replace("_", " ").split(" ");
				if (removeTablePrefix) {
					splits = ArrayUtils.subarray(splits, 1, splits.length);
				}
				String className = StringUtils.remove(WordUtils.capitalize(StringUtils.join(splits, " ")), " ");

				String fileName = tableSchema + "\\" + pojoPackage.replace(".", "\\") + "\\" + className + ".java";
				File file = new File(fileName);
				if (file.exists()) {
					file.delete();
				}
				file.createNewFile();
				StringBuffer contentBuffer = new StringBuffer();
				contentBuffer.append("package ")
					.append(pojoPackage).append(";\n\nimport java.util.*;\n\n")
					.append("/**\n * ").append(className).append(" ")
					.append(tableComment).append("\n * @author Anshen\n")
					.append(" */\n").append("public class ")
					.append(className).append(" {\n\n");
				StringBuffer getterSetterBuffer = new StringBuffer();
				String csql = "select COLUMN_NAME,DATA_TYPE,COLUMN_COMMENT from columns where TABLE_SCHEMA = ? and TABLE_NAME = ?";
				PreparedStatement pstmColumns = connection.prepareStatement(csql);
				pstmColumns.setString(1, tableSchema);
				pstmColumns.setString(2, tableName);
				pstmColumns.execute();
				ResultSet columns = pstmColumns.getResultSet();
				while (columns.next()) {
					String propertyName = WordUtils.capitalize(columns.getString("COLUMN_NAME").replace("-", " ").replace("_", " ")).replace(" ", "");
					String filedName = StringUtils.uncapitalize(propertyName);
					String dataType = columns.getString("DATA_TYPE");
					String columnComment = columns.getString("COLUMN_COMMENT");
					if (dataType.contains("int")) {
						contentBuffer.append(tabSpaces).append("private Integer ").append(filedName).append("; // ").append(columnComment).append("\n");
						getterSetterBuffer.append(tabSpaces).append("public Integer get").append(propertyName).append("() {\n")
							.append(tabSpaces).append(tabSpaces).append("return ").append(filedName).append(";\n").append(tabSpaces).append("}\n")
							.append(tabSpaces).append("public void set")
							.append(propertyName).append("(").append("Integer ").append(filedName).append(") {\n")
							.append(tabSpaces).append(tabSpaces).append("this.").append(filedName).append(" = ").append(filedName).append(";\n").append(tabSpaces)
							.append("}\n");
					} else if (dataType.contains("double")) {
						contentBuffer.append(tabSpaces).append("private Double ").append(filedName).append("; // ").append(columnComment).append("\n");
						getterSetterBuffer.append(tabSpaces).append("public Double get").append(propertyName).append("() {\n")
							.append(tabSpaces).append(tabSpaces).append("return ").append(filedName).append(";\n").append(tabSpaces).append("}\n")
							.append(tabSpaces).append("public void set")
							.append(propertyName).append("(").append("Double ").append(filedName).append(") {\n")
							.append(tabSpaces).append(tabSpaces).append("this.").append(filedName).append(" = ").append(filedName).append(";\n").append(tabSpaces)
							.append("}\n");
					} else if (dataType.contains("time") || dataType.contains("date")) {
						contentBuffer.append(tabSpaces).append("private Date ").append(filedName).append("; // ").append(columnComment).append("\n");
						getterSetterBuffer.append(tabSpaces).append("public Date get").append(propertyName).append("() {\n")
							.append(tabSpaces).append(tabSpaces).append("return ").append(filedName).append(";\n").append(tabSpaces).append("}\n")
							.append(tabSpaces).append("public void set")
							.append(propertyName).append("(").append("Date ").append(filedName).append(") {\n")
							.append(tabSpaces).append(tabSpaces).append("this.").append(filedName).append(" = ").append(filedName).append(";\n").append(tabSpaces)
							.append("}\n");
					} else {
						contentBuffer.append(tabSpaces).append("private String ").append(filedName).append("; // ").append(columnComment).append("\n");
						getterSetterBuffer.append(tabSpaces).append("public String get").append(propertyName).append("() {\n")
							.append(tabSpaces).append(tabSpaces).append("return ").append(filedName).append(";\n").append(tabSpaces).append("}\n")
							.append(tabSpaces).append("public void set")
							.append(propertyName).append("(").append("String ").append(filedName).append(") {\n")
							.append(tabSpaces).append(tabSpaces).append("this.").append(filedName).append(" = ").append(filedName).append(";\n").append(tabSpaces)
							.append("}\n");
					}
				}
				columns.close();
				contentBuffer.append("\n").append(getterSetterBuffer).append("}\n");
				write(file, contentBuffer.toString());
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
