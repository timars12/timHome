package com.example.core.utils

inline val CharSequence?.isEmail: Boolean get() = isMatch(REGEX_EMAIL)

inline val CharSequence?.isName: Boolean get() = isMatch(REGEX_NAME)

inline val CharSequence?.isPassword: Boolean get() = isMatch(REGEX_PASSWORD)
inline val CharSequence?.isNumber: Boolean get() = isMatch(REGEX_NUMBER)

fun CharSequence?.isMatch(regex: String): Boolean =
    !this.isNullOrEmpty() && Regex(regex).matches(this)

const val REGEX_EMAIL = "^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$" // todo
const val REGEX_NAME = "^([A-Za-z-]){1,20}\$"
const val REGEX_PASSWORD =
    "^" +
        "(?=.*[0-9])" + // at least 1 digit
        "(?=.*[A-Z])" + // at least 1 upper case
        "(?=.*[!@#$%^&+=])" + // at least 1 special symbol
        "(?=\\S+$)" + // no white spaces
        ".{8,}" + // at least 8 characters
        "$"
const val REGEX_NUMBER = "[0-9]\\d{9}"
