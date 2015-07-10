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

package com.sk89q.worldedit.internal.command;

import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.worldedit.*;
import com.sk89q.worldedit.blocks.ItemType;
import com.sk89q.worldedit.command.InsufficientArgumentsException;
import com.sk89q.worldedit.command.tool.InvalidToolBindException;
import com.sk89q.worldedit.internal.expression.ExpressionException;
import com.sk89q.worldedit.regions.RegionOperationException;
import com.sk89q.worldedit.util.command.parametric.ExceptionConverterHelper;
import com.sk89q.worldedit.util.command.parametric.ExceptionMatch;
import com.sk89q.worldedit.util.io.file.FileSelectionAbortedException;
import com.sk89q.worldedit.util.io.file.FilenameResolutionException;
import com.sk89q.worldedit.util.io.file.InvalidFilenameException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * converts WorldEdit exceptions and converts them into {@link CommandException}s.
 */
public class WorldEditExceptionConverter extends ExceptionConverterHelper {

    private static final Pattern numberFormat = Pattern.compile("^For input string: \"(.*)\"$");
    private final WorldEdit worldEdit;

    public WorldEditExceptionConverter(WorldEdit worldEdit) {
        checkNotNull(worldEdit);
        this.worldEdit = worldEdit;
    }

    @ExceptionMatch
    public void convert(NumberFormatException e) throws CommandException {
        final Matcher matcher = numberFormat.matcher(e.getMessage());

        if (matcher.matches()) {
            throw new CommandException("Number expected; string \"" + matcher.group(1)
                    + "\" given.");
        } else {
            throw new CommandException("Number expected; string given.");
        }
    }

    @ExceptionMatch
    public void convert(IncompleteRegionException e) throws CommandException {
        throw new CommandException("������ ���������� �������� ������.");
    }

    @ExceptionMatch
    public void convert(UnknownItemException e) throws CommandException {
        throw new CommandException("���� '" + e.getID() + "' �� ������.");
    }

    @ExceptionMatch
    public void convert(InvalidItemException e) throws CommandException {
        throw new CommandException(e.getMessage());
    }

    @ExceptionMatch
    public void convert(DisallowedItemException e) throws CommandException {
        throw new CommandException("���� '" + e.getID()
                + "' �������� ����������� WorldEdit.");
    }

    @ExceptionMatch
    public void convert(MaxChangedBlocksException e) throws CommandException {
        throw new CommandException("������� ����� ������ ��� ���� �������� ("
                + e.getBlockLimit() + ").");
    }

    @ExceptionMatch
    public void convert(MaxBrushRadiusException e) throws CommandException {
        throw new CommandException("������������ ������ ����� (� �������): " + worldEdit.getConfiguration().maxBrushRadius);
    }

    @ExceptionMatch
    public void convert(MaxRadiusException e) throws CommandException {
        throw new CommandException("������������ ������ (� �������): " + worldEdit.getConfiguration().maxRadius);
    }

    @ExceptionMatch
    public void convert(UnknownDirectionException e) throws CommandException {
        throw new CommandException("����������� �����������: " + e.getDirection());
    }

    @ExceptionMatch
    public void convert(InsufficientArgumentsException e) throws CommandException {
        throw new CommandException(e.getMessage());
    }

    @ExceptionMatch
    public void convert(RegionOperationException e) throws CommandException {
        throw new CommandException(e.getMessage());
    }

    @ExceptionMatch
    public void convert(ExpressionException e) throws CommandException {
        throw new CommandException(e.getMessage());
    }

    @ExceptionMatch
    public void convert(EmptyClipboardException e) throws CommandException {
        throw new CommandException("��� ����� ������ ����. ��� ������, �������� //copy.");
    }

    @ExceptionMatch
    public void convert(InvalidFilenameException e) throws CommandException {
        throw new CommandException("�������� ����� '" + e.getFilename() + "' �����������: "
                + e.getMessage());
    }

    @ExceptionMatch
    public void convert(FilenameResolutionException e) throws CommandException {
        throw new CommandException(
                "���������� ����� '" + e.getFilename() + "' �����������: " + e.getMessage());
    }

    @ExceptionMatch
    public void convert(InvalidToolBindException e) throws CommandException {
        throw new CommandException("���������� �������� ���������� � "
                + ItemType.toHeldName(e.getItemId()) + ": " + e.getMessage());
    }

    @ExceptionMatch
    public void convert(FileSelectionAbortedException e) throws CommandException {
        throw new CommandException("����� ����� �������.");
    }

    @ExceptionMatch
    public void convert(WorldEditException e) throws CommandException {
        throw new CommandException(e.getMessage(), e);
    }

}
