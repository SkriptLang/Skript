/**
 *   This file is part of Skript.
 *
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package ch.njol.util;

import org.jetbrains.annotations.Nullable;

/**
 * @author Peter G�ttinger
 *
 */
public class NotifyingReference<V>
{
  @Nullable
  private volatile V value;
  private final boolean notifyAll;
  
  public NotifyingReference(@Nullable V value, boolean notifyAll)
  {
    this.value = value;
    this.notifyAll = notifyAll;
  }
  
  public NotifyingReference(@Nullable V value)
  {
    this.value = value;
    this.notifyAll = true;
  }
  
  public NotifyingReference()
  {
    this.value = null;
    this.notifyAll = true;
  }
  
  @Nullable
  public synchronized V get()
  {
    return this.value;
  }
  
  public synchronized void set(@Nullable V newValue)
  {
    this.value = newValue;
    if (this.notifyAll) {
      notifyAll();
    } else {
      notify();
    }
  }
}
