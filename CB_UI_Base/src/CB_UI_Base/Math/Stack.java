/*
 * Copyright (C) 2015 team-cachebox.de
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
package CB_UI_Base.Math;

import CB_UI_Base.Global;

import java.util.ArrayList;

/**
 * @param <T>
 * @author Longri
 */
public class Stack<T> {
    ArrayList<T> m_list; // TODO replace with CB_List

    public Stack() {
        m_list = new ArrayList<T>();
    }

    public void push(T value) {
        m_list.add(0, value);
    }

    public T pop() {
        T temp = null;
        if (m_list.size() > 0) {
            temp = m_list.get(0);
            m_list.remove(0);
        }
        return temp;
    }

    public int size() {
        return m_list.size();
    }

    public T get(int i) {
        return m_list.get(i);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("Stack of " + m_list.get(0).getClass().getName());
        for (T t : m_list) {
            sb.append("  > " + t.toString() + Global.br);
        }
        return sb.toString();
    }

    public void clear() {
        m_list.clear();
    }

}
