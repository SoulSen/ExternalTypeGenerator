## External Type Generator

This project serves to create an outline of all classes accessible to JavaScript
in the [ChatTriggers](https://github.com/ChatTriggers/ct.js) mod. It generates
an external type of each class so it can be used in the Kotlin workspace, allowing
for modules to easily be made in Kotlin.

To build the "glue" file, run the main function in `Generator.kt`. I haven't added any
gradle magic or anything to do so, so for now you will have to load it up into 
IntelliJ and run the main function from there. If you find the motivation to actually
create a nice task for that, feel free to make a pull request, I would appreciate it.
Once it is finished building, you can find the generated `glue.kt` file in the `out`
directory. You can then copy and paste that into the glue file in the KotlinWorkspace
if you need an updated version for some reason.