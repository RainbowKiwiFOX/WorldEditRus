Компиляция
=========

Вы можете самостоятельно скомпилировать WorldEdit, если вы имеете [Java Development Kit (JDK)](http://www.oracle.com/technetwork/java/javase/downloads/index-jsp-138363.html) for Java 7 or newer. You only need one version of JDK installed.

Для этого используйте Gradle, который НЕ нуждается в скачивании. WorldEdit состоит из трёх модулей:

* `worldedit-core` основа WorldEdit
* `worldedit-bukkit` плагин Bukkit
* `worldedit-forge` мод Forge

## Сама компилияция...

### В Windows

<<<<<<< HEAD
1. Загрузите файлы WorldEdit и распакуйте их.
1. Нажмите Shift + ПКМ по папке с WorldEdit и нажмите "Открыть окно комманд".
2. `gradlew clean setupDevWorkspace`
=======
1. Shift + right click the folder with WorldEdit's files and click "Open command prompt".
2. `gradlew clean setupDecompWorkspace`
>>>>>>> c6b477297d4d6a2cd7aa0882c346241d5a68e2c7
3. `gradlew build`

### В UNIX-системах.

<<<<<<< HEAD
1. Скопируйте файлы WorldEdit к себе с помощью Git (как настоящий мужик) и перейдите в папку с ними: 
`git clone https://github.com/RainbowKiwiFOX/WorldEditRus.git`
`cd WorldEditRus`
2. `./gradlew clean setupDevWorkspace`
=======
1. In your terminal, navigate to the folder with WorldEdit's files (`cd /folder/of/worldedit/files`)
2. `./gradlew clean setupDecompWorkspace`
>>>>>>> c6b477297d4d6a2cd7aa0882c346241d5a68e2c7
3. `./gradlew build`

## Расположение файлов...

Файлы находятся:

* Ядро WorldEdit API в **worldedit-core/build/libs**
* WorldEdit для Bukkit в **worldedit-bukkit/build/libs**
* WorldEdit для Forge в **worldedit-forge/build/libs**

If you want to use WorldEdit, use the `-shadow` version.

(The -shadow version includes WorldEdit + necessary libraries.)

## Other commands

* `gradlew idea` will generate an [IntelliJ IDEA](http://www.jetbrains.com/idea/) module for the Forge module.
* `gradlew eclipse` will generate an [Eclipse](https://www.eclipse.org/downloads/) project for the Forge version.
* Use `setupCIWorkspace` instead of `setupDecompWorkspace` if you are doing this on a CI server.
