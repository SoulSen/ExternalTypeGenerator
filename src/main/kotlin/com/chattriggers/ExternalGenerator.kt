package com.chattriggers

import com.github.sarahbuisson.kotlinparser.KotlinParser
import com.github.sarahbuisson.kotlinparser.KotlinParserBaseListener

class ExternalGenerator : KotlinParserBaseListener() {
    private val classBuilder = StringBuilder()
    private var isInExternal = false

    //TODO: INNER CLASSES & PROPERTIES & FIX TOSTRING OVERRIDE

    override fun enterObjectDeclaration(ctx: KotlinParser.ObjectDeclarationContext?) {
        if (ctx == null || !isExternal(ctx.modifierList())) return
        isInExternal = true


        classBuilder.append("external object ${ctx.simpleIdentifier().Identifier()} ")

        if (ctx.delegationSpecifiers() != null) {
            classBuilder.append(getSupers(ctx.delegationSpecifiers()))
        }

        classBuilder.append("{\n")
    }

    override fun enterClassDeclaration(ctx: KotlinParser.ClassDeclarationContext?) {
        if (ctx == null || !isExternal(ctx.modifierList())) return
        isInExternal = true

        val isAbstract = ctx.modifierList()?.modifier()?.any {
            it?.inheritanceModifier()?.ABSTRACT() != null
        } ?: false

        if (isAbstract) classBuilder.append("abstract ")

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

        val paramList = ctx.functionValueParameters()?.functionValueParameter()?.joinToString {
            transformParam(it)
        } ?: ""

        classBuilder.append(TAB)
        classBuilder.append("constructor(")
        classBuilder.append(paramList)
        classBuilder.append(")\n")
    }

    override fun enterFunctionDeclaration(ctx: KotlinParser.FunctionDeclarationContext?) {
        if (ctx == null || !isInExternal) return

        val mods = ctx.modifierList()

        val isPrivate = mods?.modifier()?.any {
            it?.visibilityModifier()?.PRIVATE() != null
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

        classBuilder.append(TAB)

        if (isOverride) {
            classBuilder.append("override ")
        }

        classBuilder.append("fun $name($paramList)")

        if (!isUnit) {
            val fixedType = fixType(ctx.type()[0].text)
            classBuilder.append(": $fixedType")
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

        val name = param.simpleIdentifier().Identifier() ?: "def"
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

    private fun fixType(type: String) = if (BLOCKED_TYPES.contains(type)) "dynamic" else type

    private fun isExternal(modList: KotlinParser.ModifierListContext?): Boolean {
        return modList?.annotations()?.any {
            it?.annotation()?.LabelReference()?.symbol?.text == "@External"
        } ?: false
    }

    private fun exitClass() {
        if (isInExternal){
            classBuilder.append("}\n")
            isInExternal = false
        }
    }

    private fun getSupers(ctx: KotlinParser.DelegationSpecifiersContext): String {
        val supers = StringBuilder()

        supers.append(": ")
        val size = ctx.delegationSpecifier().size

        for ((i, value) in ctx.delegationSpecifier().withIndex()) {
            val invoke = value.constructorInvocation()
            val name = invoke.userType().text
            val args = invoke.callSuffix().valueArguments().text

            supers.append("$name$args")

            if (i >= size - 1) supers.append(", ")
        }

        return supers.toString()
    }

    fun build() = classBuilder.toString()
}

class Gay(private vararg val gay: Gay)