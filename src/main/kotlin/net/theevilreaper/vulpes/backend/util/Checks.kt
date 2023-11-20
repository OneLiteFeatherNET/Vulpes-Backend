package net.theevilreaper.vulpes.backend.util

/**
 * The class contains some checks methods which checks a value to a specific condition.
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 */
object Checks {

    private val nameSpacePattern = Regex("minecraft:")

    private const val minAllowedNumber = 0

    /**
     * Checks if a given number is negativ.
     * @param value the value to check
     * @return true when the value is negativ otherwise false
     */
    fun isNumberNegativ(value: Int): Boolean {
        return value < minAllowedNumber
    }

    /**
     * Checks if a given string value contains the Namespacekey for minecraft.
     * @param value the string to check
     * @return true when the string matches the key otherwise false
     */
    fun hasMinecraftKey(value: String): Boolean {
        return nameSpacePattern.matches(value)
    }
}