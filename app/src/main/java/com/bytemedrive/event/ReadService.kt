package com.bytemedrive.event

import com.google.gson.Gson
import org.reflections.Reflections
import java.io.File
import java.nio.file.FileSystems
import java.nio.file.Files
import kotlin.jvm.internal.Reflection
import kotlin.reflect.typeOf

object ReadService {
    var eventMap : Map<String, Class<*>>

    init {
        eventMap = Reflections("com.bytemedrive.upload")
            .getTypesAnnotatedWith(StoreEvent::class.java)
            .associateBy { clazz: Class<*>? -> clazz?.getAnnotation(StoreEvent::class.java)?.name!! }
    }

    fun convertToAppEvent(eventMapWrapper: EventMapWrapper): Any? {
        val classes = getClassesWithAnnotation(StoreEvent::class.java, "com.bytemedrive")

        if (eventMap.containsKey(eventMapWrapper.eventName)) {
            val json = Gson().toJson(eventMapWrapper.data)

            return Gson().fromJson(json, eventMap[eventMapWrapper.eventName])
        }

        return null
    }



    fun getClassesWithAnnotation(annotationClass: Class<out Annotation>, packageName: String): Set<Class<*>> {
        val classLoader = Thread.currentThread().contextClassLoader
        val packagePath = packageName.replace('.', '/')
        val rootPath = classLoader.getResource(packagePath)?.toURI()?.let { uri ->
            if (uri.scheme == "jar") {
                FileSystems.newFileSystem(uri, emptyMap<String, Any>()).getPath(packagePath)
            } else {
                File(uri).toPath()
            }
        } ?: throw IllegalArgumentException("Package not found: $packageName")

        val classes = mutableSetOf<Class<*>>()
        Files.walk(rootPath).forEach { path ->
            if (path.toString().endsWith(".class")) {
                val className = packageName + "." + path.fileName.toString().removeSuffix(".class")
                try {
                    val clazz = Class.forName(className, false, classLoader)
                    if (clazz.isAnnotationPresent(annotationClass)) {
                        classes.add(clazz)
                    }
                } catch (e: ClassNotFoundException) {
                    // ignore
                }
            }
        }
        return classes
    }
}