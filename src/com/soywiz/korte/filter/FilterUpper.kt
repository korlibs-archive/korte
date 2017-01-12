package com.soywiz.korte.filter

import com.soywiz.korte.Filter
import com.soywiz.korte.util.Dynamic

val FilterUpper = Filter("upper") { subject, _ -> Dynamic.toString(subject).toUpperCase() }
