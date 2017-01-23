package com.soywiz.korte.filter

import com.soywiz.korte.Filter
import com.soywiz.korte.toDynamicList

val FilterReverse = Filter("reverse") { subject, _ ->
	(subject as? String)?.reversed() ?: subject.toDynamicList().reversed()
}
