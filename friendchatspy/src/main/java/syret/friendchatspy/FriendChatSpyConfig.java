/*
 * Copyright (c) 2018, Andrew EP | ElPinche256 <https://github.com/ElPinche256>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package syret.friendchatspy;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("FriendChatSpyConfig")

public interface FriendChatSpyConfig extends Config {
    @ConfigItem(
            keyName = "webhook",
            name = "Webhook Url",
            description = "Input the url for your webhook.",
            position = 0,
            secret = true
    )
    default String webhook() {
        return "";
    }

    @ConfigItem(
            keyName = "onlywhitelisted",
            name = "only whitelisted messages",
            description = "if true only send messages with whitelisted words to discord.",
            position = 1,
            secret = false
    )
    default boolean onlySendWhitelistedMessages() {
        return false;
    }

    @ConfigItem(
            keyName = "whitelistedwords",
            name = "whitelisted words",
            description = "put the whitelisted words in here separated with commas",
            position = 2,
            secret = false
    )
    default String whiteListedWords() {
        return "";
    }

    @ConfigItem(
            keyName = "hoptoworld",
            name = "Hop to the callers world",
            description = "if true the account will hop to the world of the message sender.",
            position = 3,
            secret = false
    )
    default boolean hopToWorld() {
        return false;
    }

    @ConfigItem(
            keyName = "hopbacktoworld",
            name = "hop back to world",
            description = "if true it will hop back to a certain world after hopping.",
            position = 4,
            secret = false
    )
    default boolean hopBackToWorld() {
        return false;
    }

    @ConfigItem(
            keyName = "hopbacktoworldnumber",
            name = "hop back to world number",
            description = "set the world to hop back to",
            position = 5,
            secret = false
    )
    default int hopBackWorldNumber() {
        return 328;
    }

    @ConfigItem(
            keyName = "hopbacktoworldticks",
            name = "hop back to world ticks",
            description = "how many ticks to wait before logging back",
            position = 6,
            secret = false
    )
    default int hopBackWorldWaitTicks() {
        return 10;
    }

    @ConfigItem(
            keyName = "onlyscoutonaccount",
            name = "only scout on specific account",
            description = "if true only scouts on a specific account",
            position = 7,
            secret = false
    )
    default boolean onlyscoutonaccount() {
        return false;
    }

    @ConfigItem(
            keyName = "onlyscoutaccountname",
            name = "account name",
            description = "put the account name of the scout in here",
            position = 8,
            secret = false
    )
    default String onlyscoutaccountname() {
        return "";
    }
}