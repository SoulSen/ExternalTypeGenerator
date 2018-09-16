package com.chattriggers

import com.github.sarahbuisson.kotlinparser.KotlinLexer
import org.antlr.v4.runtime.tree.ParseTreeWalker
import com.github.sarahbuisson.kotlinparser.KotlinParser
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.ListBranchCommand
import java.io.File

fun main(args: Array<String>) {
    val files = File("./files/")
    val compiler = StringBuilder()
    files.deleteRecursively()

    val git = Git.cloneRepository()
            .setURI("https://github.com/ChatTriggers/ct.js.git")
            .setBranchesToClone(listOf("refs/heads/feature/kotlin"))
            .setBranch("refs/heads/feature/kotlin")
            .setDirectory(files)
            .call()

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