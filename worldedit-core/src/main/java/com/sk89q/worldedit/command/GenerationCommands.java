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
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.minecraft.util.commands.Logging;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.function.pattern.Patterns;
import com.sk89q.worldedit.internal.annotation.Selection;
import com.sk89q.worldedit.internal.expression.ExpressionException;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.TreeGenerator;
import com.sk89q.worldedit.util.TreeGenerator.TreeType;
import com.sk89q.worldedit.util.command.binding.Range;
import com.sk89q.worldedit.util.command.binding.Switch;
import com.sk89q.worldedit.util.command.binding.Text;
import com.sk89q.worldedit.util.command.parametric.Optional;
import com.sk89q.worldedit.world.biome.BaseBiome;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.sk89q.minecraft.util.commands.Logging.LogMode.*;

/**
 * Commands for the generation of shapes and other objects.
 */
public class GenerationCommands {

    private final WorldEdit worldEdit;

    /**
     * Create a new instance.
     *
     * @param worldEdit reference to WorldEdit
     */
    public GenerationCommands(WorldEdit worldEdit) {
        checkNotNull(worldEdit);
        this.worldEdit = worldEdit;
    }

    @Command(
        aliases = { "/hcyl" },
        usage = "<материал> <радиус>[,<ещё радиус>] [размер]",
        desc = "Создание полого цилиндра.",
        help =
            "Создание полого цилиндра.\n" +
            "При указании двух радиусов через запятую,\n" +
            "вы можете создавать эллептические цилиндры.\n" +
            "Первый радиус север/юг, второй - запад/восток.",
        min = 2,
        max = 3
    )
    @CommandPermissions("worldedit.generation.cylinder")
    @Logging(PLACEMENT)
    public void hcyl(Player player, LocalSession session, EditSession editSession, Pattern pattern, String radiusString, @Optional("1") int height) throws WorldEditException {
        cyl(player, session, editSession, pattern, radiusString, height, true);
    }

    @Command(
        aliases = { "/cyl" },
        usage = "<материал> <радиус>[,<ещё радиус>] [размер]",
        flags = "h",
        desc = "Создание цилиндров.",
        help =
            "Создание цилиндров.\n" +
            "При указании двух радиусов через запятую,\n" +
            "вы можете создавать эллептические цилиндры.\n" +
            "Первый радиус север/юг, второй - запад/восток.",
        min = 2,
        max = 3
    )
    @CommandPermissions("worldedit.generation.cylinder")
    @Logging(PLACEMENT)
    public void cyl(Player player, LocalSession session, EditSession editSession, Pattern pattern, String radiusString, @Optional("1") int height, @Switch('h') boolean hollow) throws WorldEditException {
        String[] radii = radiusString.split(",");
        final double radiusX, radiusZ;
        switch (radii.length) {
        case 1:
            radiusX = radiusZ = Math.max(1, Double.parseDouble(radii[0]));
            break;

        case 2:
            radiusX = Math.max(1, Double.parseDouble(radii[0]));
            radiusZ = Math.max(1, Double.parseDouble(radii[1]));
            break;

        default:
            player.printError("Вы можете указать 1 или 2 значения радиусов.");
            return;
        }

        worldEdit.checkMaxRadius(radiusX);
        worldEdit.checkMaxRadius(radiusZ);
        worldEdit.checkMaxRadius(height);

        Vector pos = session.getPlacementPosition(player);
        int affected = editSession.makeCylinder(pos, Patterns.wrap(pattern), radiusX, radiusZ, height, !hollow);
        player.print(affected + " блока(ов) было создано.");
    }

    @Command(
        aliases = { "/hsphere" },
        usage = "<материал> <радиус>[,<ещё радиус>,<и ещё радиус>] [рельеф?]",
        desc = "Создание пустой сферы.",
        help =
            "Создание пустой сферы.\n" +
            "При указании трёх радиусов через запятую,\n" +
            "вы можете создавать эллептические эллипсойды.\n" +
            "Первый радиус север/юг, второй - верх/низ, третий - запад/восток.",
        min = 2,
        max = 3
    )
    @CommandPermissions("worldedit.generation.sphere")
    @Logging(PLACEMENT)
    public void hsphere(Player player, LocalSession session, EditSession editSession, Pattern pattern, String radiusString, @Optional("false") boolean raised) throws WorldEditException {
        sphere(player, session, editSession, pattern, radiusString, raised, true);
    }

    @Command(
        aliases = { "/sphere" },
        usage = "<материал> <радиус>[,<ещё радиус>,<и ещё радиус>] [рельеф?]",
        flags = "h",
        desc = "Создание заполненной сферы.",
        help =
            "Создание пустой сферы.\n" +
            "При указании трёх радиусов через запятую,\n" +
            "вы можете создавать эллептические эллипсойды.\n" +
            "Первый радиус север/юг, второй - верх/низ, третий - запад/восток.",
        min = 2,
        max = 3
    )
    @CommandPermissions("worldedit.generation.sphere")
    @Logging(PLACEMENT)
    public void sphere(Player player, LocalSession session, EditSession editSession, Pattern pattern, String radiusString, @Optional("false") boolean raised, @Switch('h') boolean hollow) throws WorldEditException {
        String[] radii = radiusString.split(",");
        final double radiusX, radiusY, radiusZ;
        switch (radii.length) {
        case 1:
            radiusX = radiusY = radiusZ = Math.max(1, Double.parseDouble(radii[0]));
            break;

        case 3:
            radiusX = Math.max(1, Double.parseDouble(radii[0]));
            radiusY = Math.max(1, Double.parseDouble(radii[1]));
            radiusZ = Math.max(1, Double.parseDouble(radii[2]));
            break;

        default:
            player.printError("Вы можете указать 1 или 3 значения радиусов.");
            return;
        }

        worldEdit.checkMaxRadius(radiusX);
        worldEdit.checkMaxRadius(radiusY);
        worldEdit.checkMaxRadius(radiusZ);

        Vector pos = session.getPlacementPosition(player);
        if (raised) {
            pos = pos.add(0, radiusY, 0);
        }

        int affected = editSession.makeSphere(pos, Patterns.wrap(pattern), radiusX, radiusY, radiusZ, !hollow);
        player.findFreePosition();
        player.print(affected + " блока(ов) было создано.");
    }

    @Command(
        aliases = { "forestgen" },
        usage = "[размер] [тип] [густота]",
        desc = "Посадка леса",
        min = 0,
        max = 3
    )
    @CommandPermissions("worldedit.generation.forest")
    @Logging(POSITION)
    @SuppressWarnings("deprecation")
    public void forestGen(Player player, LocalSession session, EditSession editSession, @Optional("10") int size, @Optional("tree") TreeType type, @Optional("5") double density) throws WorldEditException {
        density = density / 100;
        int affected = editSession.makeForest(session.getPlacementPosition(player), size, density, new TreeGenerator(type));
        player.print(affected + " деревьев было посажено.");
    }

    @Command(
        aliases = { "pumpkins" },
        usage = "[размер]",
        desc = "Generate pumpkin patches",
        min = 0,
        max = 1
    )
    @CommandPermissions("worldedit.generation.pumpkins")
    @Logging(POSITION)
    public void pumpkins(Player player, LocalSession session, EditSession editSession, @Optional("10") int apothem) throws WorldEditException {
        int affected = editSession.makePumpkinPatches(session.getPlacementPosition(player), apothem);
        player.print(affected + " тыкв(ы) было посажено.");
    }

    @Command(
            aliases = { "/hpyramid" },
            usage = "<материал> <размер>",
            desc = "Создание пустой пирамиды",
            min = 2,
            max = 2
    )
    @CommandPermissions("worldedit.generation.pyramid")
    @Logging(PLACEMENT)
    public void hollowPyramid(Player player, LocalSession session, EditSession editSession, Pattern pattern, @Range(min = 1) int size) throws WorldEditException {
        pyramid(player, session, editSession, pattern, size, true);
    }

    @Command(
        aliases = { "/pyramid" },
        usage = "<материал> <размер>",
        flags = "h",
        desc = "Создание заполненной пирамиды",
        min = 2,
        max = 2
    )
    @CommandPermissions("worldedit.generation.pyramid")
    @Logging(PLACEMENT)
    public void pyramid(Player player, LocalSession session, EditSession editSession, Pattern pattern, @Range(min = 1) int size, @Switch('h') boolean hollow) throws WorldEditException {
        Vector pos = session.getPlacementPosition(player);
        worldEdit.checkMaxRadius(size);
        int affected = editSession.makePyramid(pos, Patterns.wrap(pattern), size, !hollow);
        player.findFreePosition();
        player.print(affected + " блока(ов) было установлено.");
    }

    @Command(
        aliases = { "/generate", "/gen", "/g" },
        usage = "<block> <expression>",
        desc = "Generates a shape according to a formula.",
        help =
            "Generates a shape according to a formula that is expected to\n" +
            "return positive numbers (true) if the point is inside the shape\n" +
            "Optionally set type/data to the desired block.\n" +
            "Flags:\n" +
            "  -h to generate a hollow shape\n" +
            "  -r to use raw minecraft coordinates\n" +
            "  -o is like -r, except offset from placement.\n" +
            "  -c is like -r, except offset selection center.\n" +
            "If neither -r nor -o is given, the selection is mapped to -1..1\n" +
            "See also tinyurl.com/wesyntax.",
        flags = "hroc",
        min = 2,
        max = -1
    )
    @CommandPermissions("worldedit.generation.shape")
    @Logging(ALL)
    public void generate(Player player, LocalSession session, EditSession editSession,
                         @Selection Region region,
                         Pattern pattern,
                         @Text String expression,
                         @Switch('h') boolean hollow,
                         @Switch('r') boolean useRawCoords,
                         @Switch('o') boolean offset,
                         @Switch('c') boolean offsetCenter) throws WorldEditException {

        final Vector zero;
        Vector unit;

        if (useRawCoords) {
            zero = Vector.ZERO;
            unit = Vector.ONE;
        } else if (offset) {
            zero = session.getPlacementPosition(player);
            unit = Vector.ONE;
        } else if (offsetCenter) {
            final Vector min = region.getMinimumPoint();
            final Vector max = region.getMaximumPoint();

            zero = max.add(min).multiply(0.5);
            unit = Vector.ONE;
        } else {
            final Vector min = region.getMinimumPoint();
            final Vector max = region.getMaximumPoint();

            zero = max.add(min).multiply(0.5);
            unit = max.subtract(zero);

            if (unit.getX() == 0) unit = unit.setX(1.0);
            if (unit.getY() == 0) unit = unit.setY(1.0);
            if (unit.getZ() == 0) unit = unit.setZ(1.0);
        }

        try {
            final int affected = editSession.makeShape(region, zero, unit, Patterns.wrap(pattern), expression, hollow);
            player.findFreePosition();
            player.print(affected + " block(s) have been created.");
        } catch (ExpressionException e) {
            player.printError(e.getMessage());
        }
    }

    @Command(
        aliases = { "/generatebiome", "/genbiome", "/gb" },
        usage = "<block> <expression>",
        desc = "Sets biome according to a formula.",
        help =
            "Generates a shape according to a formula that is expected to\n" +
            "return positive numbers (true) if the point is inside the shape\n" +
            "Optionally set type/data to the desired block.\n" +
            "Flags:\n" +
            "  -h to generate a hollow shape\n" +
            "  -r to use raw minecraft coordinates\n" +
            "  -o is like -r, except offset from placement.\n" +
            "  -c is like -r, except offset selection center.\n" +
            "If neither -r nor -o is given, the selection is mapped to -1..1\n" +
            "See also tinyurl.com/wesyntax.",
        flags = "hroc",
        min = 2,
        max = -1
    )
    @CommandPermissions({"worldedit.generation.shape", "worldedit.biome.set"})
    @Logging(ALL)
    public void generateBiome(Player player, LocalSession session, EditSession editSession,
                              @Selection Region region,
                              BaseBiome target,
                              @Text String expression,
                              @Switch('h') boolean hollow,
                              @Switch('r') boolean useRawCoords,
                              @Switch('o') boolean offset,
                              @Switch('c') boolean offsetCenter) throws WorldEditException {
        final Vector zero;
        Vector unit;

        if (useRawCoords) {
            zero = Vector.ZERO;
            unit = Vector.ONE;
        } else if (offset) {
            zero = session.getPlacementPosition(player);
            unit = Vector.ONE;
        } else if (offsetCenter) {
            final Vector min = region.getMinimumPoint();
            final Vector max = region.getMaximumPoint();

            zero = max.add(min).multiply(0.5);
            unit = Vector.ONE;
        } else {
            final Vector min = region.getMinimumPoint();
            final Vector max = region.getMaximumPoint();

            zero = max.add(min).multiply(0.5);
            unit = max.subtract(zero);

            if (unit.getX() == 0) unit = unit.setX(1.0);
            if (unit.getY() == 0) unit = unit.setY(1.0);
            if (unit.getZ() == 0) unit = unit.setZ(1.0);
        }

        try {
            final int affected = editSession.makeBiomeShape(region, zero, unit, target, expression, hollow);
            player.findFreePosition();
            player.print("" + affected + " columns affected.");
        } catch (ExpressionException e) {
            player.printError(e.getMessage());
        }
    }

}
