{
  description = "Shulker Vault Minecraft Mod Dev Environment";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-unstable";
    flake-utils.url = "github:numtide/flake-utils";
    nixgl.url = "github:nix-community/nixGL";
  };

  outputs =
    {
      self,
      nixpkgs,
      flake-utils,
      nixgl,
    }:
    flake-utils.lib.eachDefaultSystem (
      system:
      let
        pkgs = import nixpkgs {
          inherit system;
          overlays = [ nixgl.overlay ];
        };
      in
      {
        devShells.default = pkgs.mkShell {
          name = "shulker-vault";

          packages = with pkgs; [
            # Java 21 for Minecraft 1.21.1
            jdk21
            openal

            # Build tool
            gradle

            # nixGL for OpenGL on NixOS (AMD RX 7900 XTX)
            nixgl.packages.${system}.nixGLDefault

            # Useful extras
            git
            curl
          ];

          shellHook = ''
            echo "🏺 Shulker Vault Dev Environment"
            echo "Java: $(java -version 2>&1 | head -1)"
            echo ""
            echo "Commands:"
            echo "  ./gradlew :neoforge:runClient  → Run NeoForge client (use nixGL wrapper below)"
            echo "  ./gradlew :fabric:runClient    → Run Fabric client"
            echo "  ./gradlew build                → Build all"
            echo "  ./gradlew clean                → Clean"
            echo ""
            echo "For NixOS GPU fix, run the client with:"
            echo "  nixGL ./gradlew :neoforge:runClient"
            echo ""

            export JAVA_HOME="${pkgs.jdk21}"
            export GRADLE_OPTS="-Dorg.gradle.daemon=false"
            export LD_LIBRARY_PATH=${pkgs.openal}/lib:${pkgs.libpulseaudio}/lib:$LD_LIBRARY_PATH
          '';
        };

        # Convenience app for running the NeoForge client with nixGL
        apps.runClient = flake-utils.lib.mkApp {
          drv = pkgs.writeShellScriptBin "run-client" ''
            ${
              nixgl.packages.${system}.nixGLDefault
            }/bin/nixGLDefault ${pkgs.gradle}/bin/gradle :neoforge:runClient "$@"
          '';
        };
      }
    );
}
