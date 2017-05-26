#!groovy

import jenkins.model.*
import hudson.security.*
import jenkins.security.s2m.*
  
// create admin account
def instance = Jenkins.getInstance()
def hudsonRealm = new HudsonPrivateSecurityRealm(false)
hudsonRealm.createAccount("admin", "##PWD##")
instance.setSecurityRealm(hudsonRealm)

def strategy = new FullControlOnceLoggedInAuthorizationStrategy()
instance.setAuthorizationStrategy(strategy)
instance.save()

// disable CLI
def CLIConfig = jenkins.CLI.get()
CLIConfig.setEnabled(false)

// Agent Master ACL
instance.injector.getInstance(AdminWhitelistRule.class).setMasterKillSwitch(false);
instance.save()

// install plugins
def installed = false
def initialised = false
def pluginsString = "githug build-pipeline-plugin dashboard-view workflow-aggregator"
def plugins = pluginsString.split()
def pm = instance.getPluginManager()
def uc = instance.getUpdateCenter()

plugins.each() {
  if (!pm.getPlugin(it)) {
    if (!initialised) {
      uc.updateAllSites()
      initialised = true
    }
    
    def plugin = uc.getPlugin(it)
    if (plugin) {
      plugin.deploy()
      installed = true
    }
  }
}

if (installed) {
  instance.save()
}
