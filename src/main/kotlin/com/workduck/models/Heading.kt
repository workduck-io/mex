package com.workduck.models

enum class HeadingType {
	H1,
	H2
}

data class Heading(
	private var id: String = "",
	private var content: String = "",
	val type: HeadingType
) : Element {
	override fun getContent(): String = content

	override fun getID(): String = id

	override fun getChildren(): List<Element> {
		TODO("Not yet implemented")
	}

	override fun getType(): String {
		TODO("Not yet implemented")
	}

	fun getElementType(): String {
		TODO("Not yet implemented")
	}
}

