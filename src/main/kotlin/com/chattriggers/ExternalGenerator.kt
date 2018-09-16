package com.chattriggers

import com.github.sarahbuisson.kotlinparser.KotlinParser
import com.github.sarahbuisson.kotlinparser.KotlinParserBaseListener

class ExternalGenerator : KotlinParserBaseListener() {
    private val classBuilder = StringBuilder()
    private var isInExternal = false
    private var isInInner = false
    private var ignoreFuns = false
    private var hasDynCtor = false
    private var hasNoSupers = false

    //TODO: PROPERTIES

    override fun enterObjectDeclaration(ctx: KotlinParser.ObjectDeclarationContext?) {
        if (ctx == null) return

        if (isInExternal) {
            doInnerObject(ctx)
            return
        }

        if (!isExternal(ctx.modifierList())) {
            isInExternal = false
            return
        }

        isInExternal = true

        classBuilder.append("external object ${ctx.simpleIdentifier().Identifier()} ")

        if (ctx.delegationSpecifiers() != null) {
            classBuilder.append(getSupers(ctx.delegationSpecifiers()))
        }

        classBuilder.append("{\n")
    }

    override fun enterClassDeclaration(ctx: KotlinParser.ClassDeclarationContext?) {
        if (ctx == null) return

        if (isInExternal) {
            doInnerClass(ctx)
            return
        }

        if (!isExternal(ctx.modifierList())) {
            isInExternal = false
            return
        }

        isInExternal = true

        val isAbstract = ctx.modifierList()?.modifier()?.any {
            it?.inheritanceModifier()?.ABSTRACT() != null
        } ?: false

        val isOpen = ctx.modifierList()?.modifier()?.any {
            it?.inheritanceModifier()?.OPEN() != null
        } ?: false

        if (isAbstract) classBuilder.append("abstract ")
        if (isOpen) classBuilder.append("open ")

        classBuilder.append("external class ${ctx.simpleIdentifier().Identifier()}")

        if (ctx.primaryConstructor() != null) {
            classBuilder.append("(")

            val paramList = ctx.primaryConstructor().classParameters().classParameter().joinToString {
                transformClassParam(it)
            }

            classBuilder.append(paramList)

            classBuilder.append(") ")
        } else {
            classBuilder.append(" ")
        }

        if (ctx.delegationSpecifiers() != null) {
            classBuilder.append(getSupers(ctx.delegationSpecifiers()))
        }

        classBuilder.append("{\n")
    }

    override fun enterSecondaryConstructor(ctx: KotlinParser.SecondaryConstructorContext?) {
        if (ctx == null || !isInExternal) return

        var paramList = ctx.functionValueParameters()?.functionValueParameter()?.joinToString {
            transformParam(it)
        } ?: ""

        val isDyn = paramList.contains("dynamic") && !paramList.contains(", ")

        if (isDyn && hasDynCtor) return

        if (isDyn) {
            hasDynCtor = true
            paramList = "any: dynamic"
        }

        classBuilder.append(TAB)
        classBuilder.append("constructor(")
        classBuilder.append(paramList)
        classBuilder.append(")\n")
    }

    override fun enterFunctionDeclaration(ctx: KotlinParser.FunctionDeclarationContext?) {
        if (ctx == null || !isInExternal || ignoreFuns) return

        val mods = ctx.modifierList()

        val isPrivate = mods?.modifier()?.any {
            it?.visibilityModifier()?.PRIVATE() != null || it?.visibilityModifier()?.INTERNAL() != null
        } ?: false

        val isAbstract = mods?.modifier()?.any {
            it?.inheritanceModifier()?.ABSTRACT() != null
        } ?: false

        val isOpen = mods?.modifier()?.any {
            it?.inheritanceModifier()?.OPEN() != null
        } ?: false

        val name = ctx.identifier()?.simpleIdentifier()?.get(0)?.Identifier() ?: return

        if (isPrivate) return

        val paramList = ctx.functionValueParameters()?.functionValueParameter()?.joinToString {
            transformParam(it)
        }
        val isUnit = ctx.COLON() == null
        val isOverride = mods?.modifier()?.any {
            it?.memberModifier()?.OVERRIDE() != null
        } ?: false
        val isStringLiteral = ctx.functionBody()?.expression()?.text?.startsWith("\"") ?: false
        val isToString = name.text.contains("toString")

        classBuilder.append(TAB)

        if (isInInner) classBuilder.append(TAB)

        if (isOverride && !hasNoSupers) classBuilder.append("override ")
        if (isAbstract) classBuilder.append("abstract ")
        if (isOpen) classBuilder.append("open ")

        classBuilder.append("fun $name($paramList)")

        if (!isUnit) {
            val fixedType = fixType(ctx.type()[0].text)
            classBuilder.append(": $fixedType")
        } else if (isStringLiteral || isToString) {
            classBuilder.append(": String")
        }

        classBuilder.append("\n")
    }

    override fun exitObjectDeclaration(ctx: KotlinParser.ObjectDeclarationContext?) {
        exitClass()
    }

    override fun exitClassDeclaration(ctx: KotlinParser.ClassDeclarationContext?) {
        exitClass()
    }

    private fun transformClassParam(ctx: KotlinParser.ClassParameterContext): String {
        val isVararg = ctx.modifierList()?.modifier()?.any {
            it?.parameterModifier()?.VARARG() != null
        } ?: false

        val name = ctx.simpleIdentifier().Identifier()
        val type = ctx.type().text

        val fixedType = fixType(type)

        val hasDefault = ctx.ASSIGNMENT() != null

        val sb = StringBuilder()

        if (isVararg) sb.append("vararg ")

        sb.append("$name: $fixedType")

        if (hasDefault) {
            sb.append(" = definedExternally")
        }

        return sb.toString()
    }

    private fun transformParam(ctx: KotlinParser.FunctionValueParameterContext): String {
        val isVararg = ctx.modifierList()?.modifier()?.any {
            it?.parameterModifier()?.VARARG() != null
        } ?: false

        val param = ctx.parameter()

        val name = param.simpleIdentifier().text ?: "unknown"
        val type = param.type().text

        val fixedType = fixType(type)

        val hasDefault = ctx.ASSIGNMENT() != null

        val sb = StringBuilder()

        if (isVararg) sb.append("vararg ")
        sb.append("$name: $fixedType")

        if (hasDefault) {
            sb.append(" = definedExternally")
        }

        return sb.toString()
    }

    private fun doInnerObject(ctx: KotlinParser.ObjectDeclarationContext) {
        val isPrivate = ctx.modifierList()?.modifier()?.any {
            it?.visibilityModifier()?.PRIVATE() != null || it?.visibilityModifier()?.INTERNAL() != null
        } ?: false

        if (isPrivate) {
            ignoreFuns = true
            return
        }

         isInInner = true

        classBuilder.append(TAB)
        classBuilder.append("object ${ctx.simpleIdentifier().Identifier()} {\n")
    }

    private fun doInnerClass(ctx: KotlinParser.ClassDeclarationContext) {
        val isPrivate = ctx.modifierList()?.modifier()?.any {
            it?.visibilityModifier()?.PRIVATE() != null || it?.visibilityModifier()?.INTERNAL() != null
        } ?: false

        if (isPrivate) {
            ignoreFuns = true
            return
        }

        isInInner = true

        if (ctx.enumClassBody() != null) {
            doEnumClass(ctx)
            return
        }

        val isAbstract = ctx.modifierList()?.modifier()?.any {
            it?.inheritanceModifier()?.ABSTRACT() != null
        } ?: false

        val isOpen = ctx.modifierList()?.modifier()?.any {
            it?.inheritanceModifier()?.OPEN() != null
        } ?: false

        classBuilder.append(TAB)
        if (isAbstract) classBuilder.append("abstract ")
        if (isOpen) classBuilder.append("open ")

        classBuilder.append("class ${ctx.simpleIdentifier().Identifier()}")

        if (ctx.primaryConstructor() != null) {
            classBuilder.append("(")

            val paramList = ctx.primaryConstructor().classParameters().classParameter().joinToString {
                transformClassParam(it)
            }

            classBuilder.append(paramList)

            classBuilder.append(") ")
        } else {
            classBuilder.append(" ")
        }

        if (ctx.delegationSpecifiers() != null) {
            classBuilder.append(getSupers(ctx.delegationSpecifiers()))
        }

        classBuilder.append("{\n")
    }

    private fun doEnumClass(ctx: KotlinParser.ClassDeclarationContext) {
        val name = ctx.simpleIdentifier().text

        val entries = ctx.enumClassBody().enumEntries().enumEntry().joinToString {
            it.simpleIdentifier().text
        }

        classBuilder.append(TAB)
        classBuilder.append("enum class $name {\n")
        classBuilder.append(TAB * 2)
        classBuilder.append("$entries;\n")
    }

    private fun fixType(type: String) = if (BLOCKED_TYPES.contains(type.replace("?", ""))) "dynamic" else type

    private fun isExternal(modList: KotlinParser.ModifierListContext?): Boolean {
        return modList?.annotations()?.any {
            it?.annotation()?.LabelReference()?.symbol?.text == "@External"
        } ?: false
    }

    private fun exitClass() {
        ignoreFuns = false
        hasNoSupers = false

        if (isInInner) {
            isInInner = false
            classBuilder.append("$TAB}\n")
        } else if (isInExternal){
            isInExternal = false
            classBuilder.append("}\n")
        }

    }

    private fun getSupers(ctx: KotlinParser.DelegationSpecifiersContext): String {
        val supers = StringBuilder()

        supers.append(": ")
        val size = ctx.delegationSpecifier().size

        for ((i, value) in ctx.delegationSpecifier().withIndex()) {
            val invoke = value.constructorInvocation()
            val name = invoke.userType().text

            if (fixType(name) == "dynamic") {
                hasNoSupers = true
                return " "
            }

            supers.append(name)

            if (i < size - 1) supers.append(", ") else supers.append(" ")
        }

        return supers.toString()
    }

    fun build() = classBuilder.toString()
}