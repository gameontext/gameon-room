/*******************************************************************************
 * Copyright (c) 2015 IBM Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package net.wasdev.gameon.room;

import java.util.ArrayList;

import javax.json.JsonValue;

public class Message {

	/**
	 * Strip off segments by leading comma, stop
	 * as soon as a { is reached (beginning of JSON payload)
	 * @param message Message to split
	 * @return Array containing parts of original message
	 */
	public static final String[] splitRouting(String message) {
		ArrayList<String> list = new ArrayList<>();

		int brace = message.indexOf('{');
		int i = 0;
		int j = message.indexOf(',');
		while (j > 0 && j < brace) {
			list.add(message.substring(i, j));
			i = j+1;
			j = message.indexOf(',', i);
		}
		list.add(message.substring(i));

		return list.toArray(new String[]{});
	}
	
	//strips quotes to extract the value of the string, returns the unmodified string if it is not bookended by "
	public static String getValue(String text) {
		int start = text.indexOf('"');
		int end = text.lastIndexOf('"');
		if((start != -1) && (end != -1)) {
			return text.substring(start + 1, end);
		}
		return text;
	}
	
	public static String getValue(JsonValue value) {
		return getValue(value.toString());
	}
}
