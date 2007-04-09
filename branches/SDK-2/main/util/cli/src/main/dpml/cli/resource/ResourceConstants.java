/*
 * Copyright 2005 The Apache Software Foundation
 * Copyright 2005 Stephen McConnell
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
package dpml.cli.resource;

/**
 * Common resurce constants.
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public abstract class ResourceConstants
{
   /**
    * Bad classname constant.
    */
    public static final String CLASSVALIDATOR_BAD_CLASSNAME = "ClassValidator.bad.classname";
    
   /**
    * Class not found constant.
    */
    public static final String CLASSVALIDATOR_CLASS_NOTFOUND = "ClassValidator.class.notfound";
    
   /**
    * Class access constant.
    */
    public static final String CLASSVALIDATOR_CLASS_ACCESS = "ClassValidator.class.access";
    
   /**
    * Class creation constant.
    */
    public static final String CLASSVALIDATOR_CLASS_CREATE = "ClassValidator.class.create";
    
   /**
    * Date out of range constant.
    */
    public static final String DATEVALIDATOR_DATE_OUTOFRANGE = "DateValidator.date.OutOfRange";
    
   /**
    * Malformed uri constant.
    */
    public static final String URIVALIDATOR_MALFORMED_URI = "URIValidator.malformed.URI";
    
   /**
    * Malformed url constant.
    */
    public static final String URLVALIDATOR_MALFORMED_URL = "URLValidator.malformed.URL";
    
   /**
    * Number out-of-range constant.
    */
    public static final String NUMBERVALIDATOR_NUMBER_OUTOFRANGE =
        "NumberValidator.number.OutOfRange";
    
   /**
    * Unexpected argument value constant.
    */
    public static final String ARGUMENT_UNEXPECTED_VALUE = "Argument.unexpected.value";
    
   /**
    * Minimum greater than maximum error constant.
    */
    public static final String ARGUMENT_MIN_EXCEEDS_MAX = "Argument.minimum.exceeds.maximum";

   /**
    * Too few defaults error constant.
    */
    public static final String ARGUMENT_TOO_FEW_DEFAULTS = "Argument.too.few.defaults";
    
   /**
    * Too many defaults error constant.
    */
    public static final String ARGUMENT_TOO_MANY_DEFAULTS = "Argument.too.many.defaults";
    
   /**
    * Missing argument error constant.
    */
    public static final String ARGUMENT_MISSING_VALUES = "Argument.missing.values";
    
   /**
    * Too many argument values error constant.
    */
    public static final String ARGUMENT_TOO_MANY_VALUES = "Argument.too.many.values";
    
   /**
    * Missing trigger prefix error constant.
    */
    public static final String OPTION_TRIGGER_NEEDS_PREFIX = "Option.trigger.needs.prefix";
    
   /**
    * Missing required option error constant.
    */
    public static final String OPTION_MISSING_REQUIRED = "Option.missing.required";
    
   /**
    * Missing option name error constant.
    */
    public static final String OPTION_NO_NAME = "Option.no.name";
    
   /**
    * Illegal long prefix error constant.
    */
    public static final String OPTION_ILLEGAL_LONG_PREFIX = "Option.illegal.long.prefix";
    
   /**
    * Illegal short prefix error constant.
    */
    public static final String OPTION_ILLEGAL_SHORT_PREFIX = "Option.illegal.short.prefix";
    
   /**
    * Unexpected token error constant.
    */
    public static final String UNEXPECTED_TOKEN = "Unexpected.token";
    
   /**
    * Missing option error constant.
    */
    public static final String MISSING_OPTION = "Missing.option";
    
   /**
    * Cannot burst error constant.
    */
    public static final String CANNOT_BURST = "Cannot.burst";
    
   /**
    * Preferenced commmand name too long error constant.
    */
    public static final String COMMAND_PREFERRED_NAME_TOO_SHORT = "Command.preferredName.too.short";
    
   /**
    * Preferenced commmand name too long error constant.
    */
    public static final String SWITCH_ILLEGAL_ENABLED_PREFIX = "Option.illegal.enabled.prefix";
    
   /**
    * Illegal disabled prefix error constant.
    */
    public static final String SWITCH_ILLEGAL_DISABLED_PREFIX = "Option.illegal.disabled.prefix";
    
   /**
    * Illegal duplicate prefix error constant.
    */
    public static final String SWITCH_IDENTICAL_PREFIXES = "Option.identical.prefixes";
    
   /**
    * Switch already set error constant.
    */
    public static final String SWITCH_ALREADY_SET = "Switch.already.set";
    
   /**
    * No enabled prefix error constant.
    */
    public static final String SWITCH_NO_ENABLED_PREFIX = "Switch.no.enabledPrefix";
    
   /**
    * No disabled prefix error constant.
    */
    public static final String SWITCH_NO_DISABLED_PREFIX = "Switch.no.disabledPrefix";
    
   /**
    * Switch enabled starts with disabled error constant.
    */
    public static final String SWITCH_ENABLED_STARTS_WITH_DISABLED =
        "Switch.enabled.startsWith.disabled";
    
   /**
    * Switch disabled starts with enabled error constant.
    */
    public static final String SWITCH_DISABLED_STARTWS_WITH_ENABLED =
        "Switch.disabled.startsWith.enabled";
    
   /**
    * Switch preferred name too short error constant.
    */
    public static final String SWITCH_PREFERRED_NAME_TOO_SHORT = "Switch.preferredName.too.short";
    
   /**
    * Source dest must enforce values error constant.
    */
    public static final String SOURCE_DEST_MUST_ENFORCE_VALUES = "SourceDest.must.enforce.values";
    
   /**
    * Gutter too long error constant.
    */
    public static final String HELPFORMATTER_GUTTER_TOO_LONG = "HelpFormatter.gutter.too.long";
    
   /**
    * Width too narrow error constant.
    */
    public static final String HELPFORMATTER_WIDTH_TOO_NARROW = "HelpFormatter.width.too.narrow";
    
   /**
    * Illegal emumeration value error constant.
    */
    public static final String ENUM_ILLEGAL_VALUE = "Enum.illegal.value";
    
   /**
    * Null consume remaining error constant.
    */
    public static final String ARGUMENT_BUILDER_NULL_CONSUME_REMAINING = "ArgumentBuilder.null.consume.remaining";
    
   /**
    * Empty consume remaining error constant.
    */
    public static final String ARGUMENT_BUILDER_EMPTY_CONSUME_REMAINING = "ArgumentBuilder.empty.consume.remaining";
    
   /**
    * Null default error constant.
    */
    public static final String ARGUMENT_BUILDER_NULL_DEFAULT = "ArgumentBuilder.null.default";
    
   /**
    * Null defaults error constant.
    */
    public static final String ARGUMENT_BUILDER_NULL_DEFAULTS = "ArgumentBuilder.null.defaults";
    
   /**
    * Negative maximum error constant.
    */
    public static final String ARGUMENT_BUILDER_NEGATIVE_MAXIMUM = "ArgumentBuilder.negative.maximum";
    
   /**
    * Negative minimum error constant.
    */
    public static final String ARGUMENT_BUILDER_NEGATIVE_MINIMUM = "ArgumentBuilder.negative.minimum";
    
   /**
    * Null name error constant.
    */
    public static final String ARGUMENT_BUILDER_NULL_NAME = "ArgumentBuilder.null.name";
    
   /**
    * Empty name error constant.
    */
    public static final String ARGUMENT_BUILDER_EMPTY_NAME = "ArgumentBuilder.empty.name";
    
   /**
    * Null validator argument error constant.
    */
    public static final String ARGUMENT_BUILDER_NULL_VALIDATOR = "ArgumentBuilder.null.validator";

}
