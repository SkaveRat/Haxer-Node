package net.skaverat.haxlernode

class Main {



    companion object {
        val renderer = Renderer()
        @JvmStatic fun main(args: Array<String>) {
            while (true) {
                renderer.fetchMessage()
            }
        }
    }

}