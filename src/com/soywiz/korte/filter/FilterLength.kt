package com.soywiz.korte.filter

import com.soywiz.korte.Filter
import com.soywiz.korte.util.Dynamic

val FilterLength = Filter("length") { subject, _ -> Dynamic.length(subject) }