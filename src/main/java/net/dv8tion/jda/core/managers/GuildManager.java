/*
 *     Copyright 2015-2018 Austin Keener & Michael Ritter & Florian Spieß
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

package net.dv8tion.jda.core.managers;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.Region;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Icon;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.exceptions.GuildUnavailableException;
import net.dv8tion.jda.core.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.core.managers.impl.ManagerBase;
import net.dv8tion.jda.core.requests.Requester;
import net.dv8tion.jda.core.requests.Route;
import net.dv8tion.jda.core.utils.Checks;
import okhttp3.RequestBody;
import org.json.JSONObject;

import javax.annotation.CheckReturnValue;

/**
 * Facade for a {@link net.dv8tion.jda.core.managers.GuildManagerUpdatable GuildManagerUpdatable} instance.
 * <br>Simplifies managing flow for convenience.
 *
 * <p>This decoration allows to modify a single field by automatically building an update {@link net.dv8tion.jda.core.requests.RestAction RestAction}
 */
public class GuildManager extends ManagerBase
{
    public static final int NAME   = 0x1;
    public static final int REGION = 0x2;
    public static final int ICON   = 0x4;
    public static final int SPLASH = 0x8;
    public static final int AFK_CHANNEL    = 0x10;
    public static final int AFK_TIMEOUT    = 0x20;
    public static final int SYSTEM_CHANNEL = 0x40;
    public static final int MFA_LEVEL      = 0x80;
    public static final int NOTIFICATION_LEVEL     = 0x100;
    public static final int EXPLICIT_CONTENT_LEVEL = 0x200;
    public static final int VERIFICATION_LEVEL     = 0x400;

    protected final Guild guild;

    protected String name;
    protected String region;
    protected Icon icon;
    protected Icon splash;
    protected String afkChannel;
    protected String systemChannel;
    protected int afkTimeout;
    protected int mfaLevel;
    protected int notificationLevel;
    protected int explicitContentLevel;
    protected int verificationLevel;

    public GuildManager(Guild guild)
    {
        super(guild.getJDA(), Route.Guilds.MODIFY_GUILD.compile(guild.getId()));
        this.guild = guild;
    }

    /**
     * The {@link net.dv8tion.jda.core.JDA JDA} instance of this Manager
     *
     * @return the corresponding JDA instance
     */
    public JDA getJDA()
    {
        return guild.getJDA();
    }

    /**
     * The {@link net.dv8tion.jda.core.entities.Guild Guild} object of this Manager.
     * Useful if this Manager was returned via a create function
     *
     * @return The {@link net.dv8tion.jda.core.entities.Guild Guild} of this Manager
     */
    public Guild getGuild()
    {
        return guild;
    }

    @Override
    @CheckReturnValue
    public GuildManager reset(int fields)
    {
        super.reset(fields);
        //clear icon fields due to memory intensive footprint
        if ((fields & ICON) == ICON)
            icon = null;
        if ((fields & SPLASH) == SPLASH)
            splash = null;
        return this;
    }

    @Override
    @CheckReturnValue
    public GuildManager reset(int... fields)
    {
        super.reset(fields);
        return this;
    }

    @Override
    @CheckReturnValue
    public GuildManager reset()
    {
        super.reset();
        icon = null;
        splash = null;
        return this;
    }

    /**
     * Sets the name of this {@link net.dv8tion.jda.core.entities.Guild Guild}.
     *
     * @param  name
     *         The new name for this {@link net.dv8tion.jda.core.entities.Guild Guild}
     *
     * @throws IllegalArgumentException
     *         If the provided name is {@code null} or not between 2-100 characters long
     *
     * @return GuildManager for chaining convenience
     */
    @CheckReturnValue
    public GuildManager setName(String name)
    {
        Checks.notNull(name, "Name");
        Checks.check(name.length() >= 2 && name.length() <= 100, "Name must be between 2-100 characters long");
        this.name = name;
        set |= NAME;
        return this;
    }

    /**
     * Sets the {@link net.dv8tion.jda.core.Region Region} of this {@link net.dv8tion.jda.core.entities.Guild Guild}.
     *
     * @param  region
     *         The new region for this {@link net.dv8tion.jda.core.entities.Guild Guild}
     *
     * @throws IllegalArgumentException
     *         If the provided region is a {@link net.dv8tion.jda.core.Region#isVip() VIP Region} but the guild does not support VIP regions.
     *         Use {@link net.dv8tion.jda.core.entities.Guild#getFeatures() Guild#getFeatures()} to check if VIP regions are supported.
     *
     * @return GuildManager for chaining convenience
     *
     * @see    net.dv8tion.jda.core.Region#isVip()
     * @see    net.dv8tion.jda.core.entities.Guild#getFeatures()
     */
    @CheckReturnValue
    public GuildManager setRegion(Region region)
    {
        Checks.notNull(region, "Region");
        Checks.check(region != Region.UNKNOWN, "Region must not be UNKNOWN");
        Checks.check(!region.isVip() || guild.getFeatures().contains("VIP_REGIONS"), "Cannot set a VIP voice region on this guild");
        this.region = region.getKey();
        set |= REGION;
        return this;
    }

    /**
     * Sets the {@link net.dv8tion.jda.core.entities.Icon Icon} of this {@link net.dv8tion.jda.core.entities.Guild Guild}.
     *
     * @param  icon
     *         The new icon for this {@link net.dv8tion.jda.core.entities.Guild Guild}
     *
     * @return GuildManager for chaining convenience
     */
    @CheckReturnValue
    public GuildManager setIcon(Icon icon)
    {
        this.icon = icon;
        set |= ICON;
        return this;
    }

    /**
     * Sets the Splash {@link net.dv8tion.jda.core.entities.Icon Icon} of this {@link net.dv8tion.jda.core.entities.Guild Guild}.
     *
     * @param  splash
     *         The new splash for this {@link net.dv8tion.jda.core.entities.Guild Guild}
     *
     * @throws IllegalArgumentException
     *         If the guild's {@link net.dv8tion.jda.core.entities.Guild#getFeatures() features} does not include {@code INVITE_SPLASH}
     *
     * @return GuildManager for chaining convenience
     */
    @CheckReturnValue
    public GuildManager setSplash(Icon splash)
    {
        Checks.check(splash == null || guild.getFeatures().contains("INVITE_SPLASH"), "Cannot set a splash on this guild");
        this.splash = splash;
        set |= SPLASH;
        return this;
    }

    /**
     * Sets the AFK {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannel} of this {@link net.dv8tion.jda.core.entities.Guild Guild}.
     *
     * @param  afkChannel
     *         The new afk channel for this {@link net.dv8tion.jda.core.entities.Guild Guild}
     *
     * @throws IllegalArgumentException
     *         If the provided channel is not from this guild
     *
     * @return GuildManager for chaining convenience
     */
    @CheckReturnValue
    public GuildManager setAfkChannel(VoiceChannel afkChannel)
    {
        Checks.check(afkChannel == null || afkChannel.getGuild().equals(guild), "Channel must be from the same guild");
        this.afkChannel = afkChannel == null ? null : afkChannel.getId();
        set |= AFK_CHANNEL;
        return this;
    }

    /**
     * Sets the system {@link net.dv8tion.jda.core.entities.TextChannel TextChannel} of this {@link net.dv8tion.jda.core.entities.Guild Guild}.
     *
     * @param  systemChannel
     *         The new system channel for this {@link net.dv8tion.jda.core.entities.Guild Guild}
     *
     * @throws IllegalArgumentException
     *         If the provided channel is not from this guild
     *
     * @return GuildManager for chaining convenience
     */
    @CheckReturnValue
    public GuildManager setSystemChannel(TextChannel systemChannel)
    {
        Checks.check(systemChannel == null || systemChannel.getGuild().equals(guild), "Channel must be from the same guild");
        this.systemChannel = systemChannel == null ? null : systemChannel.getId();
        set |= SYSTEM_CHANNEL;
        return this;
    }

    /**
     * Sets the afk {@link net.dv8tion.jda.core.entities.Guild.Timeout Timeout} of this {@link net.dv8tion.jda.core.entities.Guild Guild}.
     *
     * @param  timeout
     *         The new afk timeout for this {@link net.dv8tion.jda.core.entities.Guild Guild}
     *
     * @throws IllegalArgumentException
     *         If the provided timeout is {@code null}
     *
     * @return GuildManager for chaining convenience
     */
    @CheckReturnValue
    public GuildManager setAfkTimeout(Guild.Timeout timeout)
    {
        Checks.notNull(timeout, "Timeout");
        this.afkTimeout = timeout.getSeconds();
        set |= AFK_TIMEOUT;
        return this;
    }

    /**
     * Sets the {@link net.dv8tion.jda.core.entities.Guild.VerificationLevel Verification Level} of this {@link net.dv8tion.jda.core.entities.Guild Guild}.
     *
     * @param  level
     *         The new Verification Level for this {@link net.dv8tion.jda.core.entities.Guild Guild}
     *
     * @throws IllegalArgumentException
     *         If the provided level is {@code null} or UNKNOWN
     *
     * @return GuildManager for chaining convenience
     */
    @CheckReturnValue
    public GuildManager setVerificationLevel(Guild.VerificationLevel level)
    {
        Checks.notNull(level, "Level");
        Checks.check(level != Guild.VerificationLevel.UNKNOWN, "Level must not be UNKNOWN");
        this.verificationLevel = level.getKey();
        set |= VERIFICATION_LEVEL;
        return this;
    }

    /**
     * Sets the {@link net.dv8tion.jda.core.entities.Guild.NotificationLevel Notification Level} of this {@link net.dv8tion.jda.core.entities.Guild Guild}.
     *
     * @param  level
     *         The new Notification Level for this {@link net.dv8tion.jda.core.entities.Guild Guild}
     *
     * @throws IllegalArgumentException
     *         If the provided level is {@code null} or UNKNOWN
     *
     * @return GuildManager for chaining convenience
     */
    @CheckReturnValue
    public GuildManager setDefaultNotificationLevel(Guild.NotificationLevel level)
    {
        Checks.notNull(level, "Level");
        Checks.check(level != Guild.NotificationLevel.UNKNOWN, "Level must not be UNKNOWN");
        this.notificationLevel = level.getKey();
        set |= NOTIFICATION_LEVEL;
        return this;
    }

    /**
     * Sets the {@link net.dv8tion.jda.core.entities.Guild.MFALevel MFA Level} of this {@link net.dv8tion.jda.core.entities.Guild Guild}.
     *
     * @param  level
     *         The new MFA Level for this {@link net.dv8tion.jda.core.entities.Guild Guild}
     *
     * @throws IllegalArgumentException
     *         If the provided level is {@code null} or UNKNOWN
     *
     * @return GuildManager for chaining convenience
     */
    @CheckReturnValue
    public GuildManager setRequiredMFALevel(Guild.MFALevel level)
    {
        Checks.notNull(level, "Level");
        Checks.check(level != Guild.MFALevel.UNKNOWN, "Level must not be UNKNOWN");
        this.mfaLevel = level.getKey();
        set |= MFA_LEVEL;
        return this;
    }

    /**
     * Sets the {@link net.dv8tion.jda.core.entities.Guild.ExplicitContentLevel Explicit Content Level} of this {@link net.dv8tion.jda.core.entities.Guild Guild}.
     *
     * @param  level
     *         The new MFA Level for this {@link net.dv8tion.jda.core.entities.Guild Guild}
     *
     * @throws IllegalArgumentException
     *         If the provided level is {@code null} or UNKNOWN
     *
     * @return GuildManager for chaining convenience
     */
    @CheckReturnValue
    public GuildManager setExplicitContentLevel(Guild.ExplicitContentLevel level)
    {
        Checks.notNull(level, "Level");
        Checks.check(level != Guild.ExplicitContentLevel.UNKNOWN, "Level must not be UNKNOWN");
        this.explicitContentLevel = level.getKey();
        set |= EXPLICIT_CONTENT_LEVEL;
        return this;
    }

    @Override
    protected RequestBody finalizeData()
    {
        //todo use checks instead
        if (!guild.isAvailable())
            throw new GuildUnavailableException();
        if (!guild.getSelfMember().hasPermission(Permission.MANAGE_SERVER))
            throw new InsufficientPermissionException(Permission.MANAGE_SERVER);

        JSONObject body = new JSONObject().put("name", guild.getName());
        if (shouldUpdate(NAME))
            body.put("name", name);
        if (shouldUpdate(REGION))
            body.put("region", region);
        if (shouldUpdate(AFK_TIMEOUT))
            body.put("afk_timeout", afkTimeout);
        if (shouldUpdate(ICON))
            body.put("icon", icon == null ? JSONObject.NULL : icon.getEncoding());
        if (shouldUpdate(SPLASH))
            body.put("splash", splash == null ? JSONObject.NULL : splash.getEncoding());
        if (shouldUpdate(AFK_CHANNEL))
            body.put("afk_channel_id", opt(AFK_CHANNEL));
        if (shouldUpdate(SYSTEM_CHANNEL))
            body.put("system_channel_id", opt(systemChannel));
        if (shouldUpdate(VERIFICATION_LEVEL))
            body.put("verification_level", verificationLevel);
        if (shouldUpdate(NOTIFICATION_LEVEL))
            body.put("default_notification_level", notificationLevel);
        if (shouldUpdate(MFA_LEVEL))
            body.put("mfa_level", mfaLevel);
        if (shouldUpdate(EXPLICIT_CONTENT_LEVEL))
            body.put("explicit_content_filter", explicitContentLevel);

        reset(); //now that we've built our JSON object, reset the manager back to the non-modified state
        return RequestBody.create(Requester.MEDIA_TYPE_JSON, body.toString());
    }
}
