/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.editor.settings.storage;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.AttributeSet;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.api.editor.settings.KeyBindingSettings;
import org.netbeans.modules.editor.settings.storage.api.CodeTemplateSettingsFactory;
import org.netbeans.modules.editor.settings.storage.api.EditorSettings;
import org.netbeans.modules.editor.settings.storage.api.FontColorSettingsFactory;
import org.netbeans.modules.editor.settings.storage.api.KeyBindingSettingsFactory;
import org.netbeans.modules.editor.settings.storage.codetemplates.CodeTemplateSettingsImpl;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.Repository;

/**
 * This class contains access methods for editor settings like font & colors
 * profiles and keymap profiles. 
 *
 * @author Jan Jancura
 */
public class EditorSettingsImpl extends EditorSettings {

    private static final Logger LOG = Logger.getLogger(EditorSettingsImpl.class.getName());
    
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    /** The name of the property change event for 'Highlighting' font and colors. */
    public static final String PROP_HIGHLIGHT_COLORINGS = "editorFontColors"; //NOI18N

    /** The name of the property change event for 'Token' font and colors. */
    public static final String PROP_TOKEN_COLORINGS = "fontColors"; //NOI18N
        
        
    /** The name of the default profile. */
    public static final String DEFAULT_PROFILE = "NetBeans"; //NOI18N
    
    // XXX: rewrite this using NbPreferences
    private static final String FATTR_CURRENT_FONT_COLOR_PROFILE = "currentFontColorProfile"; // NOI18N
    private static final String FATTR_CURRENT_KEYMAP_PROFILE = "currentKeymap";      // NOI18N

    /** Storage folder for the current font & color profile attribute. */
    private static final String EDITORS_FOLDER = "Editors"; //NOI18N
    /** Storage folder for the current keybindings profile attribute. */
    private static final String KEYMAPS_FOLDER = "Keymaps"; // NOI18N

    public static final String TEXT_BASE_MIME_TYPE = "text/base"; //NOI18N
    private static final String [] EMPTY = new String[0];
    
    private static EditorSettingsImpl instance = null;
    
    public static synchronized EditorSettingsImpl getInstance() {
        if (instance == null) {
            instance = new EditorSettingsImpl();
        }
        return instance;
    }

    // ------------------------------------------------------
    // Mime types
    // ------------------------------------------------------

    private final MimeTypesTracker topLevelMimeTypes = new MimeTypesTracker(EDITORS_FOLDER, null);
    private final HashMap<SettingsType, MimeTypesTracker> settingMimeTypes = new HashMap<SettingsType, MimeTypesTracker>();
    
    public Set<String> getAllMimeTypes () {
        return topLevelMimeTypes.getMimeTypes();
    }

    private MimeTypesTracker getMimeTypesTracker(Class settingApiClass) {
        SettingsType type = SettingsType.get(settingApiClass);
        assert type != null : "Invalid editor settings API class: " + settingApiClass; //NOI18N

        synchronized (settingMimeTypes) {
            MimeTypesTracker tracker = settingMimeTypes.get(type);
            if (tracker == null) {
                tracker = new MimeTypesTracker(EDITORS_FOLDER, type);
                settingMimeTypes.put(type, tracker);
            }
            return tracker;
        }
    }
    
    /**
     * Returns set of mimetypes.
     *
     * @return set of mimetypes
     */
    // XXX: the API should actually use Collection<String>
    public Set<String> getMimeTypes() {
        return getMimeTypesTracker(FontColorSettings.class).getMimeTypes();
    }
    
    /**
     * Returns name of language for given mime type.
     *
     * @return name of language for given mime type
     */
    public String getLanguageName (String mimeType) {
        return topLevelMimeTypes.getMimeTypeDisplayName(mimeType);
    }

    // ------------------------------------------------------
    // Profiles
    // ------------------------------------------------------

    private final HashMap<SettingsType, ProfilesTracker> settingProfiles = new HashMap<SettingsType, ProfilesTracker>();
    private ProfilesTracker getProfilesTracker(Class settingApiClass) {
        SettingsType type = SettingsType.get(settingApiClass);
        assert type != null : "Invalid editor settings API class: " + settingApiClass; //NOI18N
        
        synchronized (settingProfiles) {
            ProfilesTracker tracker = settingProfiles.get(type);
            if (tracker == null) {
                tracker = new ProfilesTracker(type, topLevelMimeTypes);
                settingProfiles.put(type, tracker);
            }
            return tracker;
        }
    }
    
    /**
     * Translates profile's display name to its Id. If the profile's display name
     * can't be translated this method will simply return the profile's display name
     * without translation.
     */
    String getInternalFontColorProfile(String profile) {
        ProfilesTracker tracker = getProfilesTracker(FontColorSettings.class);
        ProfilesTracker.ProfileDescription pd = tracker.getProfileByDisplayName(profile);
        return pd == null ? profile : pd.getId();
    }
    
    String getInternalKeymapProfile (String profile) {
        ProfilesTracker tracker = getProfilesTracker(KeyBindingSettings.class);
        ProfilesTracker.ProfileDescription pd = tracker.getProfileByDisplayName(profile);
        return pd == null ? profile : pd.getId();
    }
    
    // ------------------------------------------------------
    // Font Colors
    // ------------------------------------------------------

    /* package */ void notifyTokenFontColorChange(MimePath mimePath, String profile) {
        // XXX: this is hack, we should not abuse the event values like that
        pcs.firePropertyChange(PROP_TOKEN_COLORINGS, mimePath, profile);
    }
    
    /**
     * Gets display names of all font & color profiles.
     *
     * @return set of font & colors profiles
     */
    public Set<String> getFontColorProfiles () {
	return getProfilesTracker(FontColorSettings.class).getProfilesDisplayNames();
    }
    
    /**
     * Returns true for user defined profile.
     *
     * @param profile a profile name
     * @return true for user defined profile
     */
    public boolean isCustomFontColorProfile(String profile) {
        ProfilesTracker tracker = getProfilesTracker(FontColorSettings.class);
        ProfilesTracker.ProfileDescription pd = tracker.getProfileByDisplayName(profile);
        return pd != null && !pd.isRollbackAllowed();
    }

    // XXX: Rewrite this using NbPreferences
    private String currentFontColorProfile;
    
    /**
     * Returns name of current font & colors profile.
     *
     * @return name of current font & colors profile
     */
    public String getCurrentFontColorProfile () {
        if (currentFontColorProfile == null) {
            FileSystem fs = Repository.getDefault ().getDefaultFileSystem ();
            FileObject fo = fs.findResource (EDITORS_FOLDER);
            if (fo != null) {
                currentFontColorProfile = (String) fo.getAttribute(FATTR_CURRENT_FONT_COLOR_PROFILE);
            }
            if (currentFontColorProfile == null) {
                currentFontColorProfile = DEFAULT_PROFILE;
            }
        }
        if (!getFontColorProfiles ().contains (currentFontColorProfile)) {
            currentFontColorProfile = DEFAULT_PROFILE;
        }
        return currentFontColorProfile;
    }
    
    /**
     * Sets current font & colors profile.
     *
     * @param profile a profile name
     */
    public void setCurrentFontColorProfile (String profile) {
        String oldProfile = getCurrentFontColorProfile ();
        if (oldProfile.equals (profile)) return;

        currentFontColorProfile = profile;
        
        // Persist the change
	FileSystem fs = Repository.getDefault ().getDefaultFileSystem ();
	FileObject fo = fs.findResource (EDITORS_FOLDER);
        if (fo != null) {
            try {
                fo.setAttribute (FATTR_CURRENT_FONT_COLOR_PROFILE, profile);
            } catch (IOException ex) {
                LOG.log(Level.WARNING, "Can't persist change in current font&colors profile.", ex); //NOI18N
            }
        }

        // Notify others
        pcs.firePropertyChange (PROP_CURRENT_FONT_COLOR_PROFILE, oldProfile, currentFontColorProfile);
    }
    
    /**
     * Returns font & color defaults for given profile or null, if the profile
     * is unknown .
     *
     * @param profile a profile name
     * @return font & color defaults for given profile or null
     * 
     * @deprecated Use getFontColorSettings(new String[0]).getAllFontColors(profile) instead.
     */
    public Collection<AttributeSet> getDefaultFontColors(String profile) {
        return getFontColorSettings(new String[0]).getAllFontColors(profile);
    }
    
    /**
     * Returns default values for font & color defaults for given profile 
     * or null, if the profile is unknown.
     *
     * @param profile a profile name
     * @return font & color defaults for given profile or null
     * 
     * @deprecated Use getFontColorSettings(new String[0]).getAllFontColorsDefaults(profile) instead.
     */
    public Collection<AttributeSet> getDefaultFontColorDefaults(String profile) {
        return getFontColorSettings(new String[0]).getAllFontColorDefaults(profile);
    }
    
    /**
     * Sets font & color defaults for given profile.
     *
     * @param profile a profile name
     * @param fontColors font & color defaults to be used
     * 
     * @deprecated Use getFontColorSettings(new String[0]).setAllFontColors(profile, fontColors) instead.
     */
    public void setDefaultFontColors(String profile, Collection<AttributeSet> fontColors) {
        getFontColorSettings(new String[0]).setAllFontColors(profile, fontColors);
    }
    
    // Map (String (profile) > Map (String (category) > AttributeSet)).
    private Map<String, Map<String, AttributeSet>> highlightings = new HashMap<String, Map<String, AttributeSet>>();
    
    /**
     * Returns highlighting properties for given profile or null, if the 
     * profile is not known.
     *
     * @param profile a profile name
     * @return highlighting properties for given profile or null
     */
    public Map<String, AttributeSet> getHighlightings(String profile) {
        // translate profile name
        profile = getInternalFontColorProfile(profile);

        if (!highlightings.containsKey(profile)) {
            boolean specialProfile = profile.startsWith("test"); //NOI18N

            Map<String, AttributeSet> profileColorings = ColoringStorage.loadColorings(
                MimePath.EMPTY,
                specialProfile ? DEFAULT_PROFILE : profile,
                false,
                false
            );
            if (!specialProfile && !DEFAULT_PROFILE.equals(profile)) {
                Map<String, AttributeSet> defaultProfileColorings = ColoringStorage.loadColorings(
                    MimePath.EMPTY,
                    DEFAULT_PROFILE,
                    false,
                    false
                );

                // Add colorings from the default profile that do not exist in
                // the profileColorings. They are normally the same, but when
                // imported from previous version some colorings can be missing.
                // See #119709
                Map<String, AttributeSet> m = new HashMap<String, AttributeSet>();
                m.putAll(defaultProfileColorings);
                m.putAll(profileColorings);
                profileColorings = Collections.unmodifiableMap(m);
            }
            highlightings.put(profile, profileColorings);
        }

        Map<String, AttributeSet> h = highlightings.get(profile);
        return h == null ? Collections.<String, AttributeSet>emptyMap() : h;
    }
    
    // Map (String (profile) > Map (String (category) > AttributeSet)).
    private Map<String, Map<String, AttributeSet>> highlightingDefaults = new HashMap<String, Map<String, AttributeSet>>();
    
    /**
     * Returns defaults for highlighting properties for given profile,
     * or null if the profile is not known.
     *
     * @param profile a profile name
     * @return highlighting properties for given profile or null
     */
    public Map<String, AttributeSet> getHighlightingDefaults(String profile) {
        // translate profile name
        profile = getInternalFontColorProfile(profile);

        if (!highlightingDefaults.containsKey(profile)) {
            Map<String, AttributeSet> m = ColoringStorage.loadColorings(MimePath.EMPTY, profile, false, true);
            highlightingDefaults.put(profile, m);
        }

        Map<String, AttributeSet> h = highlightings.get(profile);
        return h == null ? Collections.<String, AttributeSet>emptyMap() : h;
    }
    
    /**
     * Sets highlighting properties for given profile.
     *
     * @param profile a profile name
     * @param highlighting a highlighting properties to be used
     */
    public void setHighlightings (
	String  profile,
	Map<String, AttributeSet> fontColors
    ) {
        // 1) translate profile name
	String internalProfile = getInternalFontColorProfile (profile);	
        
        if (fontColors == null) {
            // 2) remove coloring / revert to defaults
            ColoringStorage.deleteColorings
                (MimePath.EMPTY, internalProfile, false, false);
            highlightings.remove (internalProfile);
        } else {

            if (fontColors.equals (highlightings.get (internalProfile))) return;

            // 2) save new values to cache
            fontColors = Utils.immutize(fontColors);
            highlightings.put (internalProfile, fontColors);

            // 3) save new values to disk
            if (!internalProfile.startsWith ("test")) {
                ColoringStorage.saveColorings (
                    MimePath.EMPTY, 
                    internalProfile, 
                    false,
                    false,
                    fontColors.values ()
                );
            }
        }
        
        pcs.firePropertyChange(PROP_HIGHLIGHT_COLORINGS, MimePath.EMPTY, internalProfile);
    }  
    
    
    // ------------------------------------------------------
    // Keybindings
    // ------------------------------------------------------

    /**
     * Returns set of keymap profiles.
     *
     * @return set of font & colors profiles
     */
    // XXX: the API should actually use Collection<String>
    public Set<String> getKeyMapProfiles () {
	return getProfilesTracker(KeyBindingSettings.class).getProfilesDisplayNames();
    }
    
    /**
     * Returns true for user defined profile.
     *
     * @param profile a profile name
     * @return true for user defined profile
     */
    public boolean isCustomKeymapProfile (String profile) {
        ProfilesTracker tracker = getProfilesTracker(KeyBindingSettings.class);
        ProfilesTracker.ProfileDescription pd = tracker.getProfileByDisplayName(profile);
        return pd == null || !pd.isRollbackAllowed();
    }
    
    private String currentKeyMapProfile;
    
    /**
     * Returns name of current keymap profile.
     *
     * @return name of current keymap profile
     */
    public String getCurrentKeyMapProfile () {
        if (currentKeyMapProfile == null) {
            FileSystem fs = Repository.getDefault ().getDefaultFileSystem ();
            FileObject fo = fs.findResource (KEYMAPS_FOLDER);
            currentKeyMapProfile = fo == null ? null : (String) fo.getAttribute (FATTR_CURRENT_KEYMAP_PROFILE);
            if (currentKeyMapProfile == null)
                currentKeyMapProfile = DEFAULT_PROFILE;
        }
        return currentKeyMapProfile;
    }
    
    /**
     * Sets current keymap profile.
     *
     * @param profile a profile name
     */
    public void setCurrentKeyMapProfile (String keyMapName) {
        String oldKeyMap = getCurrentKeyMapProfile ();
        if (oldKeyMap.equals (keyMapName)) return;

        currentKeyMapProfile = keyMapName;
        
        // Persist the change
        try {
            FileSystem fs = Repository.getDefault ().getDefaultFileSystem ();
            FileObject fo = fs.findResource (KEYMAPS_FOLDER);
            if (fo == null) {
                fo = fs.getRoot ().createFolder (KEYMAPS_FOLDER);
            }
            fo.setAttribute (FATTR_CURRENT_KEYMAP_PROFILE, keyMapName);
        } catch (IOException ex) {
            LOG.log(Level.WARNING, "Can't persist change in current keybindings profile.", ex); //NOI18N
        }
        
        // Notify others
        pcs.firePropertyChange (PROP_CURRENT_KEY_MAP_PROFILE, oldKeyMap, currentKeyMapProfile);
    }
    
    // ------------------------------------------------------
    // Code Templates
    // ------------------------------------------------------

    public void notifyExpansionKeyChange() {
        // XXX: this is hack, we need to notify *all* lookups for all mime types
        // that the expansion key has changed
        pcs.firePropertyChange(CodeTemplateSettingsImpl.PROP_EXPANSION_KEY, null, null);
    }
    
    // support methods .........................................................
    
    /**
     * PropertyChangeListener registration.
     *
     * @param l a PropertyChangeListener to be registerred
     */
    public void addPropertyChangeListener (
        PropertyChangeListener l
    ) {
        pcs.addPropertyChangeListener (l);
    }
    
    /**
     * PropertyChangeListener registration.
     *
     * @param l a PropertyChangeListener to be unregisterred
     */
    public void removePropertyChangeListener (
        PropertyChangeListener l
    ) {
        pcs.removePropertyChangeListener (l);
    }
    
    /**
     * PropertyChangeListener registration.
     *
     * @param propertyName  The name of the property to listen on.
     * @param l a PropertyChangeListener to be registerred
     */
    public void addPropertyChangeListener (
        String propertyName,
        PropertyChangeListener l
    ) {
        pcs.addPropertyChangeListener (propertyName, l);
    }
    
    /**
     * PropertyChangeListener registration.
     *
     * @param propertyName  The name of the property to listen on.
     * @param l a PropertyChangeListener to be unregisterred
     */
    public void removePropertyChangeListener (
        String propertyName,
        PropertyChangeListener l
    ) {
        pcs.removePropertyChangeListener (propertyName, l);
    }

    private EditorSettingsImpl() {
        
    }
    
    public KeyBindingSettingsFactory getKeyBindingSettings (String[] mimeTypes) {
        mimeTypes = filter(mimeTypes);
        return KeyBindingSettingsImpl.get(Utils.mimeTypes2mimePath(mimeTypes));
    }

    public FontColorSettingsFactory getFontColorSettings (String[] mimeTypes) {
        mimeTypes = filter(mimeTypes);
        return FontColorSettingsImpl.get(Utils.mimeTypes2mimePath(mimeTypes));
    }
    
    public CodeTemplateSettingsFactory getCodeTemplateSettings(MimePath mimePath) {
        mimePath = filter(mimePath);
        return CodeTemplateSettingsImpl.get(mimePath);
    }
    
    private String [] filter(String [] mimeTypes) {
        if (mimeTypes.length > 0) {
            String [] filtered = mimeTypes;
    
            if (mimeTypes[0].contains(TEXT_BASE_MIME_TYPE)) {
                if (mimeTypes.length == 1) {
                    filtered = EMPTY;
                } else {
                    filtered = new String [mimeTypes.length - 1];
                    System.arraycopy(mimeTypes, 1, filtered, 0, mimeTypes.length - 1);
                }
                
                if (LOG.isLoggable(Level.INFO)) {
                    LOG.log(Level.INFO, TEXT_BASE_MIME_TYPE + " has been deprecated, use MimePath.EMPTY instead."); //, new Throwable("Stacktrace") //NOI18N
                }
                
            } else if (mimeTypes[0].startsWith("test")) {
                filtered = new String [mimeTypes.length];
                System.arraycopy(mimeTypes, 0, filtered, 0, mimeTypes.length);
                filtered[0] = mimeTypes[0].substring(mimeTypes[0].indexOf('_') + 1); //NOI18N

                LOG.log(Level.INFO, "Don't use 'test' mime type to access settings through the editor/settings/storage API!", new Throwable("Stacktrace"));
            }
            
            return filtered;
        } else {
            return mimeTypes;
        }
    }

    private MimePath filter(MimePath mimePath) {
        if (mimePath.size() > 0) {
            MimePath filtered = mimePath;
            String first = mimePath.getMimeType(0);
            
            if (first.contains(TEXT_BASE_MIME_TYPE)) {
                if (mimePath.size() == 1) {
                    filtered = MimePath.EMPTY;
                } else {
                    String path = mimePath.getPath().substring(first.length() + 1);
                    filtered = MimePath.parse(path);
                }
                
                if (LOG.isLoggable(Level.INFO)) {
                    LOG.log(Level.INFO, TEXT_BASE_MIME_TYPE + " has been deprecated, use MimePath.EMPTY instead."); //, new Throwable("Stacktrace") //NOI18N
                }
                
            } else if (first.startsWith("test")) {
                String filteredFirst = first.substring(first.indexOf('_') + 1); //NOI18N
                String path = filteredFirst + mimePath.getPath().substring(first.length() + 1);
                filtered = MimePath.parse(path);

                LOG.log(Level.INFO, "Don't use 'test' mime type to access settings through the editor/settings/storage API!", new Throwable("Stacktrace"));
            }
            
            return filtered;
        } else {
            return mimePath;
        }
    }
}
