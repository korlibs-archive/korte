package com.soywiz.korte.filter

import com.soywiz.korio.util.quote
import com.soywiz.korte.Filter
import com.soywiz.korte.util.Dynamic

val FilterQuote = Filter("quote") { subject, _ -> Dynamic.toString(subject).quote() }
