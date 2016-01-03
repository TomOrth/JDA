/**
 *    Copyright 2015-2016 Austin Keener & Michael Ritter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.dv8tion.jda.events.voice;

import net.dv8tion.jda.JDA;
import net.dv8tion.jda.entities.User;

public class VoiceMuteEvent extends GenericVoiceEvent
{
    public VoiceMuteEvent(JDA api, int responseNumber, User user)
    {
        super(api, responseNumber, user);
    }

    boolean isMuted()
    {
        return isSelfMuted() || isServerMuted();
    }

    boolean isSelfMuted()
    {
        return user.getVoiceStatus().isMuted();
    }

    boolean isServerMuted()
    {
        return user.getVoiceStatus().isServerMuted();
    }
}
