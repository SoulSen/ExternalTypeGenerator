## External Type Generator

This project serves to create an outline of all classes accessible to JavaScript
in the [ChatTriggers](https://github.com/ChatTriggers/ct.js) mod. It generates
an external type of each class so it can be used in the Kotlin workspace, allowing
for modules to easily be made in Kotlin.

To build the "glue" file, run the "createGlue" task under the "ChatTriggers" category. You can then copy and paste that into the glue file in the KotlinWorkspace if you need an updated version for some reason.
