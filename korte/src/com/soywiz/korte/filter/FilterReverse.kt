package com.soywiz.korte.filter

import com.soywiz.korio.util.Dynamic
import com.soywiz.korte.Filter

val FilterReverse = Filter("reverse") { subject, _ ->
	(subject as? String)?.reversed() ?: subject.toDynamicList().reversed()
}
