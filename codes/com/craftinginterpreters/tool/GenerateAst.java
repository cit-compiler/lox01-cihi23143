package com.craftinginterpreters.tool;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

public class GenerateAst {

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Usage: generate_ast <output directory>");
            System.exit(64);
        }
        String outputDir = args[0];
        
        List<String> exprTypes = Arrays.asList(
        "Binary   : Expr left, Token operator, Expr right",
        "Grouping : Expr expression",
        "Literal  : Object value",
        "Unary    : Token operator, Expr right"
    );
        defineAst(outputDir, "Expr", exprTypes);
    }

    private static void defineAst(
        String outputDir, String baseName, List<String> types) 
        throws IOException {
        
        String path = outputDir + "/" + baseName + ".java";
        PrintWriter writer = new PrintWriter(path, "UTF-8");

        writer.println("package com.craftinginterpreters.lox;");
        writer.println();
        writer.println("import java.util.List;");
        writer.println();
        writer.println("abstract class " + baseName + " {");
        
        // ★ ステップ3: defineAst内でVisitorインターフェースを定義
        defineVisitor(writer, baseName, types);

        writer.println();
        writer.println("  abstract <R> R accept(Visitor<R> visitor);");
        
        // 各サブクラスを定義 (ステップ5のaccept実装もここに含まれる)
        for (String type : types) {
            String className = type.split(":")[0].trim();
            String fields = type.split(":")[1].trim();
            defineType(writer, baseName, className, fields);
        }

        writer.println("}");
        writer.close();
    }
    
    // Visitor インターフェースを定義するためのメソッド
    private static void defineVisitor(
        PrintWriter writer, String baseName, List<String> types) {
        
        writer.println("  interface Visitor<R> {");

        for (String type : types) {
            String typeName = type.split(":")[0].trim();
            writer.println(" R visit" + typeName + baseName + "(" +
                typeName + " " + baseName.toLowerCase() + ");");
        }

        writer.println("  }");
    }

    private static void defineType(
        PrintWriter writer, String baseName, 
        String className, String fieldList) {
        
        // クラスヘッダー
        writer.println("  static class " + className + " extends " + baseName + " {");
        
        // コンストラクタ
        writer.println("    " + className + "(" + fieldList + ") {");
        
        String[] fields = fieldList.split(", ");
        for (String field : fields) {
            String name = field.split(" ")[1];
            writer.println("      this." + name + " = " + name + ";");
        }
        writer.println("    }");

        // ★ ステップ5: accept メソッドの実装を追加
        writer.println();
        writer.println("    @Override");
        writer.println("    <R> R accept(Visitor<R> visitor) {");
        writer.println("      return visitor.visit" + className + baseName + "(this);");
        writer.println("    }");

        // フィールドの定義
        writer.println();
        for (String field : fields) {
            writer.println("    final " + field + ";");
        }
        
        writer.println("  }");
        writer.println();
    }
}