android-codegen:
	cd taprux-android && RUST_LOG=info cargo run \
		--package taprux-core \
		--bin codegen \
		--features codegen \
		-- --language kotlin --output-dir generated

.PHONY: android-codegen