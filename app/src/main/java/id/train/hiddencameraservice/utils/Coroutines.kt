package id.train.hiddencameraservice.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

fun launchIO(job: suspend () -> Unit) {
    GlobalScope.launch(Dispatchers.IO) { job() }
}