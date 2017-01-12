package com.soywiz.korte.filter

import com.soywiz.korte.Filter
import com.soywiz.korte.util.Dynamic

val FilterCapitalize = Filter("capitalize") { subject, _ -> Dynamic.toString(subject).toLowerCase().capitalize() }