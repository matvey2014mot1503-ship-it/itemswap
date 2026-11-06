ITEM SWAP (Fabric 1.21.4) + Mod Menu

ПОЛУЧИТЬ .JAR БЕЗ УСТАНОВКИ ЧЕГО-ЛИБО (через GitHub):
1. Зарегистрируйся на github.com, создай новый репозиторий (New repository).
2. Нажми "uploading an existing file" и перетащи ВСЁ содержимое этой папки
   (вместе с папкой .github и src). Нажми Commit.
3. Вкладка Actions -> дождись зелёной галочки у "build" (1-3 минуты).
4. Открой этот запуск -> внизу Artifacts -> скачай itemswap-jar -> внутри itemswap-1.0.0.jar.

ИЛИ СОБРАТЬ У СЕБЯ: JDK 21 + Gradle 8.12, в папке выполнить:  gradle build
Готовый файл: build/libs/itemswap-1.0.0.jar

УСТАНОВКА: в папку mods положить itemswap-1.0.0.jar, Fabric API и Mod Menu.
ВАЖНО: файл Mod Menu должен называться modmenu-13.0.3.jar (убери .disabled в конце, иначе он не загрузится).

НАСТРОЙКА: Mod Menu -> Item Swap -> кнопка настроек (шестерёнка).
Клавиши: Настройки -> Управление -> "Item Swap" (или кнопка "Клавиши..." в окне настроек мода).
