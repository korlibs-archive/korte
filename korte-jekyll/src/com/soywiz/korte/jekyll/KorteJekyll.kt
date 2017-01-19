@file:Suppress("EXPERIMENTAL_FEATURE_WARNING")

package com.soywiz.korte.jekyll

import com.soywiz.korio.async.EventLoop
import com.soywiz.korio.async.WorkQueue
import com.soywiz.korio.vfs.LocalVfs

object KorteJekyll {
	val workQueue = WorkQueue()
	val base = LocalVfs("c:/projects/jekylltest")
	val inp = base["input"]
	val out = base["output"]

	suspend fun rebuild() {
		println("rebuild!")
		//for (f in inp.list()) println(f)

		for (file in inp.listRecursive {
			val basename = it.basename
			!basename.startsWith(".") &&
				basename != "_site"
		}) {
			//for (file in inp.listRecursive()) {
			println(file)
		}
		//for (file in inp.list()) {
		//	println(file)
		//}
	}

	@JvmStatic fun main(args: Array<String>) = EventLoop.main {
		inp.watch {
			workQueue { rebuild() }
		}

		workQueue { rebuild() }
	}
}

