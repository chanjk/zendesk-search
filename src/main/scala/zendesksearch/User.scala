package zendesksearch

case class User(_id: Int,
                url: String,
                externalId: String,
                name: String,
                alias: String,
                createdAt: String,
                active: Boolean,
                verified: Boolean,
                shared: Boolean,
                locale: String,
                timezone: String,
                lastLoginAt: String,
                email: String,
                phone: String,
                signature: String,
                organizationId: Int,
                tags: List[String],
                suspended: Boolean,
                role: String)
