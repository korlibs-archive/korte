package com.soywiz.korte.filter

import com.soywiz.korte.Filter
import com.soywiz.korte.util.Dynamic

val FilterReverse = Filter("reverse") { subject, _ ->
	(subject as? String)?.reversed() ?: Dynamic.toList(subject).reversed()
}
