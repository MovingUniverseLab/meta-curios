# meta-curios usage
This is used for generating SDK or Linux image for the Xiphos [Q7S](https://xiphos.com/product-details/q7) board for the CuRIOS project. Q7S is the space-qualified version of Q7, but note that Q7 is revc1 whereas Q7S is revb. The lab Xiphos is Q7. The flight Xiphos is Q7S. Generally, the recipes in this layer can also be used for other yocto projects, which requires these software packages to be pre-installed.
## Generate SDK
1. clone the Xiphos's yocto repo
  ```
  git clone --depth 1 -b xiphos-v2021.2 git@trac.xiphos.com:xiphos/yocto.git yocto-xiphos
  ```
  **Note: You have to add your ssh public to the Xiphos git system, or you won't be able to clone the repo in this way.** 

2. clone this repo to the `yocto-xiphos/yocto`
  ```
  cd yocto-xiphos/yocto
  git clone https://github.com/liuweiseu/meta-curios.git
  ```

3. create the `q7s-build` dir
  ```
  . setup-environment q7s-build
  ```
  If everything goes well, the `q7s-build` dir should be create automaticlly, and you should be in the `q7s-build` dir now.  

4. edit the conf file
  ```
  cd q7s-build
  vim conf/local.conf

  # (1) add `PREMIRRORS`
  PREMIRRORS = " \
    git://.*/.* file://${ABSOLUTE_PATH_TO_DOWNLOADS} \ 
    gitsm://.*/.* file://${ABSOLUTE_PATH_TO_DOWNLOADS} \ 
    http://.*/.* file://${ABSOLUTE_PATH_TO_DOWNLOADS} \ 
    "
  # (1) add the following line
  IMAGE_INSTALL_append = "cfitsio"
  ```
  **Note: You need to modify ${ABSOLUTE_PATH_TO_DOWNLOADS} to the absolute path of the xsc-downloads folder on your machine.**  
  Here is an example of the `conf/local.conf` on my machine:
  ```
  MACHINE ?= "q7s-revb"
  ...
  PREMIRRORS = " \
    git://.*/.* file:///home/wei/yocto-xiphos/xsc-downloads \ 
    gitsm://.*/.* file:///home/wei/yocto-xiphos/xsc-downloads \ 
    http://.*/.* file:///home/wei/yocto-xiphos/xsc-downloads \ 
    "
  IMAGE_INSTALL_append = "cfitsio"
  ```
  **Note:**   
  (1) To build the image/SDK for "q7s-revc" board (lab Xiphos), you need to set `MACHINE ?= "q7s-revc". Use q7s-revb for the flight model`;  
  (2) To install the SDK on a ARM64 platform, you need to add `SDKMACHINE = "aarch64"` to the `conf/local`.conf; if the SDK is for a x86-64 platform, you need to add `SDKMACHINE = "x86_64"`;  
  (3) There is a bug in `yocto-xiphos-v2021.1`(**v3.3.0 or earlier**), which pops up error messages during SDK generation. To fix the error, you need to add the following lines to the `conf/local/conf`:
  ```
      TOOLCHAIN_TARGET_TASK_remove = "xsc-rmap-dev"
      TOOLCHAIN_TARGET_TASK_remove += "xsc-rmap-staticdev"
      TOOLCHAIN_TARGET_TASK_remove += "libhrestime-staticdev"
  ``` 
5. add `meta-curios` layer to the project
  ```
  bitbake-layers add-layer ../meta-curios
  ```

6. generate SDK
  ```
  bitbake xsc-image-minimal -c populate_sdk
  ```
  If the SDK is generated successfully, you will see this file: `tmp-glibc/deploys/sdk/xxx.sh`.  
  The `xxx.sh` file is the sdk installer.  
  In this project, the sdk installer file is `ark-glibc-x86_64-xsc-image-minimal-cortexa9hf-neon-toolchain-nodistro.0.sh`.  
  The pre-generated SDK installer is [here](https://drive.google.com/file/d/1DrD2aojUrLQlZW1J733bs4REkvqRUPfh/view?usp=sharing).
## Use the SDK

1. install SDK    
  ```
  sudo sh ./ark-glibc-x86_64-xsc-image-minimal-cortexa9hf-neon-toolchain-nodistro.0.sh

  # You will see the following messages:
  ark (Xiphos Systems Reference Distribution) SDK installer version nodistro.0
  ============================================================================
  Enter target directory for SDK (default: /opt/xiphos/sdk/ark):
  You are about to install the SDK to "/opt/xiphos/sdk/ark". Proceed[Y/n]? Y
  Extracting SDK...............................done
  Setting it up...done
  SDK has been successfully set up and is ready to be used.
  Each time you wish to use the SDK in a new shell session, you need to source the environment setup script e.g.
  $ . /opt/xiphos/sdk/ark/environment-setup-cortexa9hf-neon-xiphos-linux-gnueabi
  ```
  The default installation path is `/opt/xiphos/sdk/ark`. You could install the SDK to anywhere you want.  
  **Note:** The SDK installer has to be copied to your host machine.

2. source the environment setup script
  ```
  . /opt/xiphos/sdk/ark/environment-setup-cortexa9hf-neon-xiphos-linux-gnueabi
  ```
  If everything goes well, you should see `$CC` is setup:
  ```
  echo $CC

  arm-xiphos-linux-gnueabi-gcc -march=armv7-a -marm -mfpu=neon -mfloat-abi=hard -mcpu=cortex-a9 --sysroot=/opt/xiphos/sdk/ark/sysroots/cortexa9hf-neon-xiphos-linux-gnueabi
  ```

3. cross compile your code with the SDK  
  Let's assume you want to cross compile `test.c`, so you just need to do:
  ```
  $CC test.c -o test
  ``` 
  Now, you should get the cross-compiled exectuble file: `test`.
