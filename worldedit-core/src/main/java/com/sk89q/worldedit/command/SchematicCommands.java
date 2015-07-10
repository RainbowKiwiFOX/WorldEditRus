/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.worldedit.command;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.util.io.file.FilenameException;
import com.sk89q.worldedit.util.io.file.FilenameResolutionException;
import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardWriter;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.transform.Transform;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.util.io.Closer;
import com.sk89q.worldedit.util.command.parametric.Optional;
import com.sk89q.worldedit.world.registry.WorldData;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Commands that work with schematic files.
 */
public class SchematicCommands {

    private static final Logger log = Logger.getLogger(SchematicCommands.class.getCanonicalName());
    private final WorldEdit worldEdit;

    /**
     * Create a new instance.
     *
     * @param worldEdit reference to WorldEdit
     */
    public SchematicCommands(WorldEdit worldEdit) {
        checkNotNull(worldEdit);
        this.worldEdit = worldEdit;
    }

    @Command(
            aliases = { "load" },
            usage = "[<формат>] <название>",
            desc = "Загрузить схему в буфер обмена",
            min = 1, max = 2
    )
    @Deprecated
    @CommandPermissions({ "worldedit.clipboard.load", "worldedit.schematic.load" })
    public void load(Player player, LocalSession session, @Optional("schematic") String formatName, String filename) throws FilenameException {
        LocalConfiguration config = worldEdit.getConfiguration();

        File dir = worldEdit.getWorkingDirectoryFile(config.saveDir);
        File f = worldEdit.getSafeOpenFile(player, dir, filename, "schematic", "schematic");

        if (!f.exists()) {
            player.printError("Схема " + filename + " не найдена!");
            return;
        }

        ClipboardFormat format = ClipboardFormat.findByAlias(formatName);
        if (format == null) {
            player.printError("Неизвестный формат схемы: " + formatName);
            return;
        }

        Closer closer = Closer.create();
        try {
            FileInputStream fis = closer.register(new FileInputStream(f));
            BufferedInputStream bis = closer.register(new BufferedInputStream(fis));
            ClipboardReader reader = format.getReader(bis);

            WorldData worldData = player.getWorld().getWorldData();
            Clipboard clipboard = reader.read(player.getWorld().getWorldData());
            session.setClipboard(new ClipboardHolder(clipboard, worldData));

            log.info(player.getName() + " загрузил схему " + f.getCanonicalPath());
            player.print("Схема '" + filename + "' загружна. Установите её, используя //paste");
        } catch (IOException e) {
            player.printError("Схема не может быть прочитана или отсутствует: " + e.getMessage());
            log.log(Level.WARNING, "Ошибка записи схемы в буфер обмена.", e);
        } finally {
            try {
                closer.close();
            } catch (IOException ignored) {
            }
        }
    }

    @Command(
            aliases = { "save" },
            usage = "[<формат>] <название>",
            desc = "Сохранить схему из буфера обмена в файл",
            min = 1, max = 2
    )
    @Deprecated
    @CommandPermissions({ "worldedit.clipboard.save", "worldedit.schematic.save" })
    public void save(Player player, LocalSession session, @Optional("schematic") String formatName, String filename) throws CommandException, WorldEditException {
        LocalConfiguration config = worldEdit.getConfiguration();

        File dir = worldEdit.getWorkingDirectoryFile(config.saveDir);
        File f = worldEdit.getSafeSaveFile(player, dir, filename, "schematic", "schematic");

        ClipboardFormat format = ClipboardFormat.findByAlias(formatName);
        if (format == null) {
            player.printError("Неизвестный формат схемы: " + formatName);
            return;
        }

        ClipboardHolder holder = session.getClipboard();
        Clipboard clipboard = holder.getClipboard();
        Transform transform = holder.getTransform();
        Clipboard target;

        // If we have a transform, bake it into the copy
        if (!transform.isIdentity()) {
            FlattenedClipboardTransform result = FlattenedClipboardTransform.transform(clipboard, transform, holder.getWorldData());
            target = new BlockArrayClipboard(result.getTransformedRegion());
            target.setOrigin(clipboard.getOrigin());
            Operations.completeLegacy(result.copyTo(target));
        } else {
            target = clipboard;
        }

        Closer closer = Closer.create();
        try {
            // Create parent directories
            File parent = f.getParentFile();
            if (parent != null && !parent.exists()) {
                if (!parent.mkdirs()) {
                    throw new CommandException("Ошибка при создании папки сохранения!");
                }
            }

            FileOutputStream fos = closer.register(new FileOutputStream(f));
            BufferedOutputStream bos = closer.register(new BufferedOutputStream(fos));
            ClipboardWriter writer = closer.register(format.getWriter(bos));
            writer.write(target, holder.getWorldData());
            log.info(player.getName() + " сохранил схему " + f.getCanonicalPath());
            player.print(filename + " сохранена.");
        } catch (IOException e) {
            player.printError("Схема не может быть записана: " + e.getMessage());
            log.log(Level.WARNING, "Ошибка при сохранении из буфера в файл", e);
        } finally {
            try {
                closer.close();
            } catch (IOException ignored) {
            }
        }
    }

    @Command(
            aliases = { "delete", "d" },
            usage = "<название>",
            desc = "Удаление файла схемы",
            help = "Удаление файла схемы",
            min = 1,
            max = 1
    )
    @CommandPermissions("worldedit.schematic.delete")
    public void delete(Player player, LocalSession session, EditSession editSession, CommandContext args) throws WorldEditException {

        LocalConfiguration config = worldEdit.getConfiguration();
        String filename = args.getString(0);

        File dir = worldEdit.getWorkingDirectoryFile(config.saveDir);
        File f = worldEdit.getSafeSaveFile(player, dir, filename, "schematic", "schematic");

        if (!f.exists()) {
            player.printError("Схема " + filename + " не найдена!");
            return;
        }

        if (!f.delete()) {
            player.printError("Ошибка удаления " + filename + ". Может быть режим 'Только чтение'?");
            return;
        }

        player.print(filename + " был удалён.");
    }

    @Command(
            aliases = {"formats", "listformats", "f"},
            desc = "Список доступных форматов",
            max = 0
    )
    @CommandPermissions("worldedit.schematic.formats")
    public void formats(Actor actor) throws WorldEditException {
        actor.print("Доступные форматы схем (Name: Lookup names)");
        StringBuilder builder;
        boolean first = true;
        for (ClipboardFormat format : ClipboardFormat.values()) {
            builder = new StringBuilder();
            builder.append(format.name()).append(": ");
            for (String lookupName : format.getAliases()) {
                if (!first) {
                    builder.append(", ");
                }
                builder.append(lookupName);
                first = false;
            }
            first = true;
            actor.print(builder.toString());
        }
    }

    @Command(
            aliases = {"list", "all", "ls"},
            desc = "Список сохранёных схем",
            max = 0,
            flags = "dn",
            help = "Список сохранёных схем\n" +
                    " -d сортировка по дате, старые сверху\n" +
                    " -n сортировка по дате, новые сверху\n"
    )
    @CommandPermissions("worldedit.schematic.list")
    public void list(Actor actor, CommandContext args) throws WorldEditException {
        File dir = worldEdit.getWorkingDirectoryFile(worldEdit.getConfiguration().saveDir);
        File[] files = dir.listFiles(new FileFilter(){
            @Override
            public boolean accept(File file) {
                // sort out directories from the schematic list
                // if WE supports sub-directories in the future,
                // this will have to be changed
                return file.isFile();
            }
        });
        if (files == null) {
            throw new FilenameResolutionException(dir.getPath(), "Папка схем не найдена.");
        }

        final int sortType = args.hasFlag('d') ? -1 : args.hasFlag('n') ? 1 : 0;
        // cleanup file list
        Arrays.sort(files, new Comparator<File>(){
            @Override
            public int compare(File f1, File f2) {
                // this should no longer happen, as directory-ness is checked before
                // however, if a directory slips through, this will break the contract
                // of comparator transitivity
                if (!f1.isFile() || !f2.isFile()) return -1;
                // http://stackoverflow.com/questions/203030/best-way-to-list-files-in-java-sorted-by-date-modified
                int result = sortType == 0 ? f1.getName().compareToIgnoreCase(f2.getName()) : // use name by default
                    Long.valueOf(f1.lastModified()).compareTo(f2.lastModified()); // use date if there is a flag
                if (sortType == 1) result = -result; // flip date for newest first instead of oldest first
                return result;
            }
        });

        actor.print("Доступные схемы (Название (формат)):");
        actor.print(listFiles("", files));
    }

    private String listFiles(String prefix, File[] files) {
        StringBuilder build = new StringBuilder();
        for (File file : files) {
            if (file.isDirectory()) {
                build.append(listFiles(prefix + file.getName() + "/", file.listFiles()));
                continue;
            }

            if (!file.isFile()) {
                continue;
            }

            build.append("\n\u00a79");
            ClipboardFormat format = ClipboardFormat.findByFile(file);
            build.append(prefix).append(file.getName()).append(": ").append(format == null ? "Неизвестно" : format.name());
        }
        return build.toString();
    }
}
