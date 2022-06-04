import processing.core.PApplet
import sun.misc.Unsafe
import java.lang.reflect.Field

fun main(){
    disableWarning()
    PApplet.main(arrayOf(DarkForestApplication::class.qualifiedName))
}

fun disableWarning() {
    try {
        val theUnsafe: Field = Unsafe::class.java.getDeclaredField("theUnsafe")
        theUnsafe.isAccessible = true
        val u: Unsafe = theUnsafe.get(null) as Unsafe
        val cls = Class.forName("jdk.internal.module.IllegalAccessLogger")
        val logger: Field = cls.getDeclaredField("logger")
        u.putObjectVolatile(cls, u.staticFieldOffset(logger), null)
    } catch (_: Exception) {}
}