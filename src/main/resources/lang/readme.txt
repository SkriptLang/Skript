====
      This file is part of Skript.

     Skript is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.

     Skript is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.

     You should have received a copy of the GNU General Public License
     along with Skript.  If not, see <http://www.gnu.org/licenses/>.

    Copyright Peter Güttinger, SkriptLang team and contributors
====

= Language Files =
.lang files are in the same format as all Skript configs & scripts.
To create a new language file it's preferable copy an existing language file and rename it to <language>.lang
and then modify the 'language' entry in your config.sk accordingly.

You can either put language files into the lang folder in the jar or into the plugins/Skript/lang/ folder, but files in the latter folder take precedence
(If there are two files for the same language both will be loaded but the file in the folder will overwrite values from the file in the jar)
The exception to this rule is the default english file which is only loaded from the jar.

Strings that have arguments use Java's formatter syntax, see https://docs.oracle.com/javase/8/docs/api/java/util/Formatter.html#syntax.

Nouns have special syntax to define their plural and gender:
	Plurals are defined like in aliases.sk: 'name¦s' / 'shel¦f¦ves' / 'word¦¦s¦ of power'
	Genders are defined by adding @<gender> at the end of the noun, e.g. 'word¦s @a' or 'ocelot¦s @an'
		(english uses a/an as genders, while other languages actually have genders)

Please make sure that the version number in your file matches the version number of the english file your
file is based off. It is used to inform the users and yourself if the file is outdated.
