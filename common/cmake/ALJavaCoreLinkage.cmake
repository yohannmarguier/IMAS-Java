# IMAS-Core still supplies the JNI wrapper's C headers and the runtime library
# opened by the shim. Do not link it in shim mode: both libraries export the
# same C ABI, and direct linkage could bypass conversion.
set( AL_JAVA_CORE_TARGET al )
set( AL_JAVA_SHIM_TEST_ENVIRONMENT "" )

if( AL_USE_MULTIVERSION_SHIM )
  find_package( imas-mvdd-loader REQUIRED CONFIG )
  set( AL_JAVA_CORE_TARGET imas-mvdd-loader::imas-mvdd-loader )

  target_include_directories( al-java-binding PRIVATE
    "$<TARGET_PROPERTY:al,INTERFACE_INCLUDE_DIRECTORIES>"
  )

  if( AL_DOWNLOAD_DEPENDENCIES OR AL_DEVELOPMENT_LAYOUT )
    # Core is built in this tree but must remain off the JNI binding's link line.
    add_dependencies( al-java-binding al )
    list( APPEND AL_JAVA_SHIM_TEST_ENVIRONMENT "IMAS_CORE_LIBRARY=$<TARGET_FILE:al>" )
  endif()

  # The shim needs the DD version used to generate this HLI to enable conversion.
  list( APPEND AL_JAVA_SHIM_TEST_ENVIRONMENT "IMAS_MVDD_HLI_DD_VERSION=${DD_VERSION}" )
  message( STATUS "Java Access Layer calls use the multiversion shim (${imas-mvdd-loader_DIR})" )
endif()
