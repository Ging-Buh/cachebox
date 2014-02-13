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

import CB_UI_Base.graphics.GL_Paint;
import CB_Utils.Lists.CB_List;

import com.badlogic.gdx.utils.Disposable;

/**
 * @author Longri
 */
public class QuadranglePath extends CB_List<Quadrangle> implements Disposable
{
	private static final long serialVersionUID = 2368800461989756291L;
	protected AtomicBoolean isDisposed = new AtomicBoolean(false);

	public QuadranglePath(PathLine pathLine, GL_Paint paint)
	{
		for (Line line : pathLine)
		{
			this.add(new Quadrangle(line, paint.getStrokeWidth()));
		}
	}

	public boolean isDisposed()
	{
		return isDisposed.get();
	}

	@Override
	public void dispose()
	{
		synchronized (isDisposed)
		{
			if (isDisposed.get()) return;
			for (Quadrangle qu : this)
			{
				qu.dispose();
			}
			this.clear();
			isDisposed.set(true);
		}
	}
}
