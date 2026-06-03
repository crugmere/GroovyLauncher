package app.groovylauncher.helper

import app.groovylauncher.data.AppModel

interface AppFilterHelper {
    fun onAppFiltered(items:List<AppModel>)
}