import type {CodegenConfig} from '@graphql-codegen/cli'

const config: CodegenConfig = {
    schema: ['../graphql/*.graphql'],
    //documents: ['src/**/*.svelte', 'src/lib/operations/**/*.graphql'],
    ignoreNoDocuments: true,
    generates: {
        './messages/src/main/java/io/temporal/ecommerce/messages/generated/Types.java': {
            plugins: ['java'],
            config: {
                package: 'io.temporal.ecommerce.messages.generated'
            }
        }
    },
}
export default config
