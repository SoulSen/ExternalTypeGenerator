package com.chattriggers

import com.github.sarahbuisson.kotlinparser.KotlinLexer
import org.antlr.v4.runtime.tree.ParseTreeWalker
import com.github.sarahbuisson.kotlinparser.KotlinParser
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.eclipse.jgit.api.Git
import java.io.File

fun main(args: Array<String>) {
    val files = File("./files/")
    val compiler = StringBuilder()
    files.deleteRecursively()

    Git.cloneRepository()
            .setURI("https://github.com/ChatTriggers/ct.js.git")
            .setBranchesToClone(listOf("refs/heads/master"))
            .setBranch("refs/heads/master")
            .setDirectory(files)
            .call()

    compiler.append("@file:Suppress(\"UNUSED\")\n\n")

    File(files, "src/main/kotlin").walk().filter {
        it.name.endsWith(".kt")
    }.forEach {
        parseFile(it, compiler)
    }

    val outFile = File("./out/glue.kt")
    outFile.createNewFile()
    outFile.writeText(compiler.toString())
}

fun parseFile(toParse: File, compiler: StringBuilder) {
    println("Parsing file ${toParse.name}")
    var text = toParse.readText()

    text = text.replace("0.0", "0")

    val lexer = KotlinLexer(CharStreams.fromString(text))

    val commonTokenStream = CommonTokenStream(lexer)
    val kotlinParser = KotlinParser(commonTokenStream)

    val tree = kotlinParser.kotlinFile()
    val walker = ParseTreeWalker()

    val listener = ExternalGenerator()
    walker.walk(listener, tree)

    compiler.append(listener.build())
}
