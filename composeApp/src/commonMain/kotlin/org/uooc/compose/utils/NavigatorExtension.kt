package org.uooc.compose.utils

import androidx.navigation.NavHostController
import org.uooc.compose.component.BasicScreen
import kotlin.reflect.KClass

fun NavHostController.push(route: KClass<out BasicScreen<*>>) {
    this.navigate(route.routeName.apply {
        println("push routeName: $this")
    })
}
val KClass<out BasicScreen<*>>.routeName: String
    get() = this.simpleName!!

fun NavHostController.replace(route: KClass<out BasicScreen<*>>) {
    this.navigate(route.routeName.apply {
        println("replace routeName: $this")
    }){
        popUpTo(this@replace.graph.startDestinationRoute!!) {
            inclusive = true
        }
    }
}
fun NavHostController.pop() {
    this.popBackStack()
}

val NavHostController.canPop: Boolean
    get() = this.currentBackStackEntry != null