package com.example.falcon.tools

data class ToolDefinition(
    val name: String,
    val description: String,
    val parametersJsonSchema: String,
    val category: ToolCategory,
    val requiredPermission: String? = null
)

enum class ToolCategory {
    SYSTEM,
    APPLICATIONS,
    MEDIA,
    WEB,
    MEMORY,
    UTILITY
}

data class ToolResult(
    val success: Boolean,
    val message: String,
    val data: Any? = null,
    val error: String? = null,
    val actionTaken: String? = null
)
