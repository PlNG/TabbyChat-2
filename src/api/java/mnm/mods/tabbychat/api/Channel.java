package mnm.mods.tabbychat.api;

/**
 * Represents a channel.
 */
public interface Channel {

    /**
     * Gets the name of this channel.
     *
     * @return The name
     */
    String getName();

    /**
     * Gets the position of the channel on the tray.
     *
     * @return The position
     */
    int getPosition();

    /**
     * Sets the position of the channel on the tray. Out of bounds numbers will
     * be made so.
     *
     * @param pos The new position
     */
    void setPosition(int pos);

    /**
     * Gets the alias that is displayed on the channel tab.
     *
     * @return The alias
     */
    String getAlias();

    /**
     * Sets the alias that is displayed on the channel tab.
     *
     * @param alias The new alias
     */
    void setAlias(String alias);

    /**
     * Gets the prefix that is inserted before the input.
     *
     * @return The prefix
     */
    String getPrefix();

    /**
     * Sets the prefix that is inserted before the input.
     *
     * @param prefix The new prefix
     */
    void setPrefix(String prefix);

    /**
     * Gets whether the prefix is hidden or not. A hidden prefix will not be
     * displayed while typing. Defaults to false.
     *
     * @return True if the prefix is hidden
     */
    boolean isPrefixHidden();

    /**
     * Sets whether the prefix is hidden or not. A hidden prefix will not be
     * displayed while typing.
     *
     * @param hidden Whether to hide the prefix
     */
    void setPrefixHidden(boolean hidden);

    /**
     * Gets whether this channel is active.
     *
     * @return Whether active or not
     */
    boolean isActive();

    /**
     * Sets whether this channel is active or not.
     *
     * @param active
     */
    void setActive(boolean active);

    /**
     * Gets whether this channel is pending.
     *
     * @return True if pending
     */
    boolean isPending();

    /**
     * Sets whether this channel is pending.
     *
     * @param pending The new pending
     */
    void setPending(boolean pending);

    /**
     * Opens the settings panel for this channel.
     */
    void openSettings();
}
