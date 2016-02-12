/* 
 * Copyright (C) 2014 team-cachebox.de
 *
 * Licensed under the : GNU General Public License (GPL);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.gnu.org/licenses/gpl.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package CB_UI_Base.graphics.Geometry;

import java.util.concurrent.atomic.AtomicBoolean;

import CB_UI_Base.graphics.Geometry.Line.SplittResult;
import CB_Utils.Lists.CB_List;

import com.badlogic.gdx.utils.Disposable;

/**
 * A Path holds in a Array of Lines
 * 
 * @author Longri
 */
public class PathLine extends CB_List<Line> implements Disposable {
	private static final long serialVersionUID = -6613969555300557959L;

	protected AtomicBoolean isDisposed = new AtomicBoolean(false);

	public PathLine(float[] coords) {
		if (coords.length < 4)
			throw new IllegalArgumentException("Line Coords can not < 4");

		if (coords.length == 4) {
			this.add(new Line(coords[0], coords[1], coords[2], coords[3]));
		} else {
			for (int i = 0; i < coords.length - 2; i += 2) {
				this.add(new Line(coords, i));
			}
		}
	}

	public PathLine() {
		// do nothing
	}

	public void splittWithDashArray(float[] dashArray) {
		// splitt
		PathLine tmp = new PathLine();

		boolean dash = false;
		int dashIndex = 0;
		int lineIndex = 0;
		float dashValue = 0;
		float rest = 0;
		Line actWorkLine = null;

		float dashLength = 0;
		for (int i = 0; i < dashArray.length; i++)
			dashLength += dashArray[i];

		do {
			if (rest >= 0) {
				if (lineIndex > this.size - 1)
					break;
				actWorkLine = this.get(lineIndex++);
			}
			if (dashValue == 0) {
				dashValue = dashArray[dashIndex++];
				dash = !dash;
				if (dashIndex >= dashArray.length) {
					dashIndex = 0;
				}
			}
			SplittResult result = actWorkLine.splitt(dashValue);

			if (dash) {
				tmp.add(result.splittLine1);
			}

			if (result.rest == -1) {
				actWorkLine = result.splittLine2;
				dashValue = 0;
			} else {
				dashValue = result.rest;
			}
			rest = result.rest;

			// safty break
			if (tmp.size > (this.getPathLength() / dashLength) * 2) {
				break;
			}

		} while (lineIndex < this.size || rest != 0);

		this.clear();
		this.addAll(tmp);

	}

	/**
	 * Returns the additional length of all Lines
	 * 
	 * @return
	 */
	public float getPathLength() {
		float l = 0;

		for (int i = 0, n = this.size(); i < n; i++) {
			l += this.get(i).length();
		}

		return l;
	}

	public boolean isDisposed() {
		return isDisposed.get();
	}

	@Override
	public void dispose() {
		synchronized (isDisposed) {
			if (isDisposed.get())
				return;

			for (int i = 0, n = this.size(); i < n; i++) {
				this.get(i).dispose();
			}
			this.clear();
			isDisposed.set(true);
		}
	}
}
