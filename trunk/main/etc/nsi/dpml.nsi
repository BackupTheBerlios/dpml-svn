; DPML NSIS Installer
; Copyright 2004 Stephen McConnell
; Copyright 2004 The Apache Software Foundation
;
; Licensed under the Apache License, Version 2.0 (the "License");
; you may not use this file except in compliance with the License.
; You may obtain a copy of the License at
;
;    http://www.apache.org/licenses/LICENSE-2.0
;
; Unless required by applicable law or agreed to in writing, software
; distributed under the License is distributed on an "AS IS" BASIS,
; WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
; See the License for the specific language governing permissions and
; limitations under the License.

!define ALL_USERS

!define TEMP $R0

!include "WriteEnvStr.nsh"
!include "PathMunge.nsh"
!include "MUI.nsh"

;--------------------------------
;Configuration

  ;General
  Name "DPML SDK @PROJECT-VERSION@"
  OutFile "dpml-sdk-@PROJECT-VERSION@.exe"

  ;Folder selection page
  InstallDir "C:\dpml"
  
  ;Remember install folder
  InstallDirRegKey HKCU "Software\DPML\@PROJECT-VERSION@" ""

;--------------------------------
;Variables

  Var MUI_TEMP
  Var STARTMENU_FOLDER

;--------------------------------
;Interface Settings

  !define MUI_HEADERIMAGE
  !define MUI_ABORTWARNING

;--------------------------------
;Pages

  !insertmacro MUI_PAGE_WELCOME
  !insertmacro MUI_PAGE_LICENSE "..\bundle\LICENSE.txt"
  !insertmacro MUI_PAGE_COMPONENTS
  !insertmacro MUI_PAGE_DIRECTORY

  ;Start Menu Folder Page Configuration
  !define MUI_STARTMENUPAGE_REGISTRY_ROOT "HKCU"
  !define MUI_STARTMENUPAGE_REGISTRY_KEY "Software\Modern UI Test"
  !define MUI_STARTMENUPAGE_REGISTRY_VALUENAME "Start Menu Folder"

  !insertmacro MUI_PAGE_STARTMENU Application $STARTMENU_FOLDER

  !insertmacro MUI_PAGE_INSTFILES
  !insertmacro MUI_PAGE_FINISH

  !insertmacro MUI_UNPAGE_WELCOME
  !insertmacro MUI_UNPAGE_CONFIRM
  !insertmacro MUI_UNPAGE_INSTFILES
  !insertmacro MUI_UNPAGE_FINISH
  
;--------------------------------
;Languages
 
  !insertmacro MUI_LANGUAGE "English"
  
;--------------------------------
;Language Strings

  ;Description
  LangString DESC_SecPlatform ${LANG_ENGLISH} "Installs Transit Resource Management, Depot Build Management, Metro Runtime and the Station Application Controller"
  LangString DESC_SecStation  ${LANG_ENGLISH} "Installs Station SCM"
  LangString DESC_SecDoc      ${LANG_ENGLISH} "Installs Platform and API Documentation"

;--------------------------------
;Installer Sections

Section "platform" SecPlatform

  SetOutPath $INSTDIR
  File  ..\bundle\README.txt
  File  ..\bundle\LICENSE.txt
  File  ..\bundle\NOTICE.txt

  Push "DPML_HOME"
  Push $INSTDIR
  Call WriteEnvStr
  
  Push $INSTDIR\share\bin
  Call AddToPath

  ;Store install folder
  WriteRegStr HKCU "Software\DPML" "" $INSTDIR

  !insertmacro MUI_STARTMENU_WRITE_BEGIN Application

    ;Create shortcuts
    CreateDirectory "$SMPROGRAMS\$STARTMENU_FOLDER"
    CreateShortCut "$SMPROGRAMS\$STARTMENU_FOLDER\Uninstall.lnk" "$INSTDIR\Uninstall.exe"

    CreateShortCut "$SMPROGRAMS\$STARTMENU_FOLDER\DPML Home (local).lnk" "$INSTDIR\share\docs\index.html"
    CreateShortCut "$SMPROGRAMS\$STARTMENU_FOLDER\DPML Platform API.lnk" "$INSTDIR\share\docs\api\dpml\@PROJECT-VERSION@\index.html"

  !insertmacro MUI_STARTMENU_WRITE_END
  
  ;Create uninstaller
  WriteUninstaller "$INSTDIR\Uninstall.exe"

  SetOutPath $INSTDIR
  File /r ..\bundle\*

SectionEnd

Section "station scm" SecStation
  Exec "$INSTDIR\share\bin\scm\wrapper.exe -i $INSTDIR\share\bin\scm\conf\wrapper.conf"
SectionEND

;--------------------------------
;Descriptions

!insertmacro MUI_FUNCTION_DESCRIPTION_BEGIN
  !insertmacro MUI_DESCRIPTION_TEXT ${SecPlatform} $(DESC_SecPlatform)
  !insertmacro MUI_DESCRIPTION_TEXT ${SecStation} $(DESC_SecStation)
!insertmacro MUI_FUNCTION_DESCRIPTION_END
 
;--------------------------------
;Uninstaller Section

Section "Uninstall"

  Exec "$INSTDIR\share\bin\scm\Wrapper.exe -r $INSTDIR\share\bin\scm\wrapper.conf"

  Push "DPML_HOME"
  Call un.DeleteEnvStr

  Push $INSTDIR\bin
  Call un.RemoveFromPath

  !insertmacro MUI_STARTMENU_GETFOLDER Application $MUI_TEMP

  Delete "$SMPROGRAMS\$MUI_TEMP\Uninstall.lnk"
  RMDir /r "$SMPROGRAMS\$MUI_TEMP"

  RMDir /r $INSTDIR
  
SectionEnd

; todo:
;    Check for JVM and Version.  Install endorsed jars if needed
;      see  http://nsis.sourceforge.net/archive/nsisweb.php?page=543&instances=0
;    Check for if we are installing for one user or many
;    Install a default app that includes the facilities

; eof
