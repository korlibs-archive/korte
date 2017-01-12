package com.soywiz.korte.filter

import com.soywiz.korte.Filter
import com.soywiz.korte.util.Dynamic

val FilterLower = Filter("lower") { subject, _ -> Dynamic.toString(subject).toLowerCase() }