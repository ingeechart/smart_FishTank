/*
 * Copyright (C) 2017 Samsung Electronics Co., Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cloud.artik.example.hellocloud;

class Config {
    static final String CLIENT_ID = "115f16f58dc14238bfbf81a912eb0bfc";//"YOUR_CLIENT_ID"; // AKA application ID
    static final String DEVICE_ID = "daafb4a24b3043cc96186ecfb95b85a1";//"YOUR_DEVICE_ID";
    static final String DID_KAKAO = "b5de7c81b39b4353be569b22c772ea9e";//kakao send to me
    static final String LED_INGEE = "26cfed8850dd4b5d9bedb76dcd496096"; //

    static final String LED_NAME = "LED-ingee"; //
    static final String LED_ID = "26cfed8850dd4b5d9bedb76dcd496096"; //

    static final String ACTION_NAME = "sub_action";
    static final String ACTION_ID ="2367b41312d94487afb30987f8eb651c";
    static final String SENSOR_NAME = "fish_share";
    static final String SENSOR_ID ="69bd256ed7974409840acf8a3cb11785";

    // MUST be consistent with "AUTH REDIRECT URL" of your application set up at the developer.artik.cloud
    static final String REDIRECT_URI = "cloud.artik.example.hellocloud://oauth2callback";

}
